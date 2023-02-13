
import Modeling.Modeling;
import Training.Training;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ModelTest {
    public static void main(String[] args) {
        double[][][] epochs = new double[30][4000][8];
        double[][] labels = new double[1][30];
        double[][] CSP_Filter = null;
        double TypeOne = 0;
        double[] w = null;
        double b = 0;
        double[][] Type = null;
        double[] filter_a = null;
        double[] filter_b = null;



        List<Double> LabelList = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < 30; i++){
            if (index < 15){
                LabelList.add(1.0);
            }else {
                LabelList.add(0.0);
            }
            index++;
        }
        //打乱
        Collections.shuffle(LabelList);

        for (int i = 0; i <LabelList.size();i++){
            labels[0][i] = LabelList.get(i);
        }
        Random random = new Random();
        for (int i = 0; i < 30; i++){
            for (int j = 0; j < 4000;j++){
                for (int k = 0; k < 8; k++){
                    epochs[i][j][k] = random.nextInt(1000);
                    System.out.println(epochs[i][j][k] + "  ");
                }
            }
        }
        File file = new File("matTest.mat");
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        for (int i = 0; i < 30; i++){
            MLDouble mlDouble = new MLDouble("T"+i,epochs[i]);
            list.add(mlDouble);
        }
        MLDouble mlDoubleLabel = new MLDouble("MyLabel",labels);
        list.add(mlDoubleLabel);
        try {
            new MatFileWriter(file,list);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Modeling model = null;
        try {
            model = new Modeling();
            Object[] result = model.MakeModel(5,epochs,labels);
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
        //训练
        try{
            Training training = new Training();
            double[] Feedback = new double[30];
            System.out.println("三十组训练结果：");
            for (int i = 0; i < 30; i++){
                Object[] objects = training.MakeTrain(1,epochs[i],CSP_Filter,TypeOne,w,b,Type);
                MWNumericArray mwNumericArray = (MWNumericArray) objects[0];
                Feedback[i] = mwNumericArray.getDouble();
                System.out.print(Feedback[i] + " ");
            }
        }catch (MWException e){
            e.printStackTrace();
        }
    }


    }

