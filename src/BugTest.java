import Modeling.Modeling;
import Training.Training;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BugTest {
    public static void main(String[] args) {
        File file = new File("ModelData11.txt");
        try {
            FileInputStream inputStream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String str = bufferedReader.readLine();
            String[] dataPack = str.trim().split("  ");
            System.out.println(dataPack.length);
            SubDataSolution subDataSolution = new SubDataSolution();
            subDataSolution.initData();
            for (int i = 0; i < dataPack.length; i++){
                    if (subDataSolution.addDataEpochs(dataPack[i])){
                        break;
                    }

                //System.out.println(dataPack[i]);
            }
            double[][][] Epochs = subDataSolution.getEpochs();
            double[][] Labels = subDataSolution.getLabels();
            double[][] CSP_Filter = null;
            double TypeOne = 0;
            double[] w = null;
            double b = 0;
            double[][] Type = null;

            Modeling model = null;
            try {
                model = new Modeling();
                Object[] result = model.MakeModel(5,Epochs,Labels);
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

            File trainFile = new File("TrainData11.txt");
            FileInputStream inputStreamTrain = new FileInputStream(trainFile);
            InputStreamReader readerTrain = new InputStreamReader(inputStreamTrain);
            BufferedReader bufferedReaderTrain = new BufferedReader(readerTrain);
            String strTrain = bufferedReaderTrain.readLine();
            String[] dataPackTrain = strTrain.trim().split("  ");
            System.out.println("训练时采集的总包数："+dataPackTrain.length);
            double[][][] trainDatas = new double[30][][];
            List<Double> feedbackList = new ArrayList<>();
            List<double[][][]> trainDataList = new ArrayList<>();
            int group = 0;
            for (int i = 0; i < dataPackTrain.length; i++){
                  if (subDataSolution.addTrainData(dataPackTrain[i])){
//                      trainDatas[group] = subDataSolution.getTrainData()[0];
//                      group++;
                      //trainDataList.add(subDataSolution.getTrainData());
                      Training training = null;
                      try {
                          training = new Training();
                          Object[] objects = training.MakeTrain(1,subDataSolution.getTrainData(),CSP_Filter,TypeOne,w,b,Type);
                          MWNumericArray mwNumericArray = (MWNumericArray) objects[0];
                          double Feedback = mwNumericArray.getDouble();
                          feedbackList.add(Feedback);
                          if (feedbackList.size() == 30){
                              System.out.println("客户端训练反馈结果"+feedbackList);
                          }
                      } catch (MWException e) {
                          e.printStackTrace();
                      }
                  }
            }
            for (int j = 0; j < trainDataList.size(); j++){

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
