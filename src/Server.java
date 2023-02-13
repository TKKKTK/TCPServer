import Modeling.Modeling;
import Training.Training;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8000;
    private List<Socket> mList = new ArrayList<Socket>();
    private ServerSocket server = null;
    private ExecutorService myExecutorService = null;


    public static void main(String[] args) {
        new Server();
    }

    public Server()
    {
        try
        {
            server = new ServerSocket(PORT);
            myExecutorService = Executors.newCachedThreadPool();
            System.out.println("服务端运行中...\n");
            Socket client = null;
            while(true)
            {
                client = server.accept();
                mList.add(client);
                myExecutorService.execute(new Service(client));
            }

        }catch(Exception e){e.printStackTrace();}
    }

    class Service implements Runnable
    {
        private Socket socket;
        private BufferedReader in = null;
        private String msg = "";
        private String currentLink = "";
        private SubDataSolution subDataSolution = null;
        double[][] CSP_Filter = null;
        double TypeOne = 0;
        double[] w = null;
        double b = 0;
        double[][] Type = null;
        List<Double> feedbackList = new ArrayList<>();
        private BufferedWriter modelOut = null;
        private BufferedWriter trainOut = null;
        private int group = 0;


        public Service(Socket socket) {
            this.socket = socket;
            try
            {
//                modelOut = new BufferedWriter(new FileWriter("ServerModelData02.txt", true)); //存放离线的txt数据
//                trainOut = new BufferedWriter(new FileWriter("ServerTrainData02.txt", true));
                subDataSolution = new SubDataSolution();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                msg = "用户:" +this.socket.getInetAddress() + "~加入"
                        +"当前在线人数:" +mList.size();
                this.sendmsg(msg);
            }catch(IOException e){e.printStackTrace();}
        }



        @Override
        public void run() {
            try{
                while(true)
                {
                    if((msg = in.readLine()) != null)
                    {
                        if(msg.trim().equals("bye"))
                        {
                            System.out.println("~~~~~~~~~~~~~");
                            mList.remove(socket);
                            in.close();
                            msg = "用户:" + socket.getInetAddress()
                                    + "退出:" +"当前在线人数:"+mList.size();
                            socket.close();
                            this.sendmsg(msg);
                            break;
                        }else{
                            if (msg.trim().equals("StartModel")){
                                currentLink = msg;
                                subDataSolution.initData();
                                this.sendmsg("StartModel");

                                continue;
                            }else if (msg.trim().equals("StopModel")){
                                //modelOut.close();
                                double[][][] Epoch = subDataSolution.getEpochs();
                                double[][] Label = subDataSolution.getLabels();
                                currentLink = "";
                            }else if (msg.trim().equals("breakDown")){ //中途退出
                                currentLink = msg;
                            }else if (msg.trim().equals("StartTrain")){ //开始训练
                                currentLink = "StartTrain";
                                continue;
                            }

                            if (currentLink.trim().equals("StartModel")){
                                //modelOut.write(msg + "  ");
                                if (subDataSolution.addDataEpochs(msg)){
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Model();
                                        }
                                    }).start();
                                }
                            }

                            if (currentLink.trim().equals("StartTrain")){

                                //trainOut.write(msg + "  ");
                                if (subDataSolution.addTrainData(msg)){
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Train();
                                        }
                                    }).start();
                                }
                            }

                            msg = socket.getInetAddress() + "   说: " + msg;
                            System.out.println(msg);
                            //this.sendmsg(msg);
                        }
                    }
                }
            }catch(Exception e){e.printStackTrace();}
        }

        /**
         * 模型数据缓存线程，调试用
         */
        class ModelCacheDataThread implements Runnable {
            @Override
            public void run() {

            }
        }

        /**
         * 训练数据缓存，调试用
         */
        class TrainCacheDataThread implements Runnable {
            @Override
            public void run() {

            }
        }

        /**
         * 训练
         */
        public void Train(){
            double[][][] trainData = subDataSolution.getTrainData();

            Training training = null;
            try {
                training = new Training();
                Object[] objects = training.MakeTrain(1,trainData[group],CSP_Filter,TypeOne,w,b,Type);
                MWNumericArray mwNumericArray = (MWNumericArray) objects[0];
                double Feedback = mwNumericArray.getDouble();
                feedbackList.add(Feedback);
                if (feedbackList.size() == 30){
                    System.out.println("服务器训练反馈结果"+feedbackList);
                    //把三十组训练结果数据存放到矩阵当中去
                    File file = new File("ServerTrainData12.mat");
                    ArrayList<MLArray> list = new ArrayList<MLArray>();
                    for (int i = 0; i < 30; i++){
                        MLDouble mlDouble = new MLDouble("Train"+i,subDataSolution.getTrainData()[i]);
                        list.add(mlDouble);
                    }
                    try {
                        new MatFileWriter(file,list);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //trainOut.close();
                }
                if (Feedback == 1.0){
                    sendmsg("bend");
                }else if (Feedback == 0.0){
                    sendmsg("failed");
                }
                group++; //训练时的组数
                group = group >= 30 ? 0 : group;
            } catch (MWException e) {
                e.printStackTrace();
            }

        }

        /**
         * 建模
         */
        public void Model(){
            double[][][] Epoch = subDataSolution.getEpochs();
            double[][] Label = subDataSolution.getLabels();

            Modeling model = null;
            try {
                model = new Modeling();
                Object[] result = model.MakeModel(5,Epoch,Label);
                for (int i = 0; i < result.length; i++){
                    MWNumericArray mwNumericArray = (MWNumericArray) result[i];
                    switch (i){
                        case 0:
                            CSP_Filter = (double[][]) mwNumericArray.toDoubleArray();
                            break;
                        case 1:
                            TypeOne = mwNumericArray.getDouble();
                            break;
                        case 2:
                            w = mwNumericArray.getDoubleData();
                            break;
                        case 3:
                            b = mwNumericArray.getDouble();
                            break;
                        case 4:
                            Type = (double[][]) mwNumericArray.toDoubleArray();
                            break;
                    }
                }
                System.out.println("第一个输出结果:"+result[0].toString());
                System.out.println("第二个输出结果:"+result[1].toString());
                System.out.println("第三个输出结果:"+result[2].toString());
                System.out.println("第四个输出结果:"+result[3].toString());
                System.out.println("第五个输出结果:"+result[4].toString());
            } catch (MWException e) {
                e.printStackTrace();
            }


            //存放矩阵数据调试用
            File file = new File("ServerModelData12.mat");
            ArrayList<MLArray> list = new ArrayList<MLArray>();
            for (int i = 0; i < 30; i++){
                MLDouble mlDouble = new MLDouble("Model"+i,Epoch[i]);
                list.add(mlDouble);
            }
            MLDouble mlDoubleLabel = new MLDouble("MyLabel",Label);
            list.add(mlDoubleLabel);
            try {
                new MatFileWriter(file,list);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        public void sendmsg(String message)
        {
            System.out.println(message);
//            int num = mList.size();
//            for(int index = 0;index < num;index++)
//            {
//                Socket mSocket = mList.get(index);
                PrintWriter pout = null;
                try {
                    pout = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream(),"UTF-8")),true);
                    pout.println(message);
                }catch (IOException e) {e.printStackTrace();}
//            }
        }
    }
}
