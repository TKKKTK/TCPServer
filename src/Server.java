import com.mathworks.toolbox.javabuilder.MWException;
import train.Modeling;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 5000;
    private List<Socket> mList = new ArrayList<Socket>();
    private ServerSocket server = null;
    private ExecutorService myExecutorService = null;
    private String currentLink = "";



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

        public Service(Socket socket) {
            this.socket = socket;
            try
            {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                msg = "用户:" +this.socket.getInetAddress() + "~加入"
                        +"当前在线人数:" +mList.size();
                this.sendmsg();
            }catch(IOException e){e.printStackTrace();}
        }



        @Override
        public void run() {
            try{
                while(true)
                {
                    if((msg = in.readLine()) != null)
                    {
                        if(msg.equals("bye"))
                        {
                            System.out.println("~~~~~~~~~~~~~");
                            mList.remove(socket);
                            in.close();
                            msg = "用户:" + socket.getInetAddress()
                                    + "退出:" +"当前在线人数:"+mList.size();
                            socket.close();
                            this.sendmsg();
                            break;
                        }else{
                            if (msg.equals("StsrtModel")){
                                currentLink = msg;
                                continue;
                            }else if (msg.equals("StopModel")){
                                currentLink = msg;
                                continue;
                            }
                            if (currentLink.equals("StsrtModel")){
                                SubDataSolution.get_instance().addDataEpochs(msg);
                            }
                            if (currentLink.equals("StopModel")){
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                         Model();
                                    }
                                }).start();
                            }
                            msg = socket.getInetAddress() + "   说: " + msg;
                            this.sendmsg();
                        }
                    }
                }
            }catch(Exception e){e.printStackTrace();}
        }

        public void Model(){
            double[][][] Epoch = SubDataSolution.get_instance().getEpochs();
            double[][] Label = SubDataSolution.get_instance().getLabels();
            Modeling train1 = null;
            try {
                train1 = new Modeling();
                Object[] result = train1.train(5,Epoch,Label);
                System.out.println("第一个输出结果:"+result[0].toString());
                System.out.println("第二个输出结果:"+result[1].toString());
                System.out.println("第三个输出结果:"+result[2].toString());
                System.out.println("第四个输出结果:"+result[3].toString());
                System.out.println("第五个输出结果:"+result[4].toString());
            } catch (MWException e) {
                e.printStackTrace();
            }

        }

        public void sendmsg()
        {
            System.out.println(msg);
//            int num = mList.size();
//            for(int index = 0;index < num;index++)
//            {
//                Socket mSocket = mList.get(index);
                PrintWriter pout = null;
                try {
                    pout = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream(),"UTF-8")),true);
                    pout.println(msg);
                }catch (IOException e) {e.printStackTrace();}
//            }
        }
    }
}
