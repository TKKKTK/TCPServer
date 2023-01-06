import com.jmatio.io.MatFileHeader;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSingle;
import train.Train;

import java.io.IOException;
import java.util.Map;

public class JavaCallMat {
    public static void main(String[] args) {
       double[][][] MyEpoch = null;
       double[][] MyLabel = null;
        MatFileReader matFileReader = null;
        try {
            matFileReader = new MatFileReader("SZW_10.mat");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 读取MAT Header信息
        MatFileHeader matFileHeader = matFileReader.getMatFileHeader();
        System.out.println("*****mat header*****");
        System.out.println(matFileHeader.toString());

        // 读取矩阵信息,通过矩阵名来获取矩阵信息
        System.out.println("*****mat body*****");

        Map<String, MLArray> content = matFileReader.getContent();

        for (String key : content.keySet()) {
            System.out.println("key=" + key);
            MLArray mlArray = content.get(key);
            System.out.println("value=" + mlArray.toString());
            System.out.println();
            if (key.equals("MyEpoch")){
                // 转换为java数组
                MyEpoch = transferMLArray(mlArray);

                // 打印数据
                int[] dimensions = mlArray.getDimensions();
                for (int k = 0; k < dimensions[2]; k++) {
                    System.out.println("第" + (k + 1) + "页数据");

                    for (int i = 0; i < dimensions[0]; i++) {
                        for (int j = 0; j < dimensions[1]; j++) {
                            System.out.print(MyEpoch[i][j][k] + "  ");
                        }
                        System.out.println();
                    }
                    System.out.println();
                }
            }else if (key.equals("MyLabel")){
                // 转换为java数组
                 MyLabel = two_dimension_transferMLArray(mlArray);

                // 打印数据
                int[] dimensions = mlArray.getDimensions();
                    for (int i = 0; i < dimensions[0]; i++) {
                        for (int j = 0; j < dimensions[1]; j++) {
                            System.out.print(MyLabel[i][j] + "  ");
                        }
                        System.out.println();
                    }
                    System.out.println();
                }
        }

        try{
            Train train1 = new Train();
            Object[] result = train1.train(5,MyEpoch,MyLabel);
            System.out.println("第一个输出结果:"+result[0].toString());
            System.out.println("第二个输出结果:"+result[1].toString());
            System.out.println("第三个输出结果:"+result[2].toString());
            System.out.println("第四个输出结果:"+result[3].toString());
            System.out.println("第五个输出结果:"+result[4].toString());
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public static double[][] two_dimension_transferMLArray(MLArray mlArray){
        int[] dimensions = mlArray.getDimensions();
        double[][] result = null;
        System.out.println("mlArray.isDouble()");
        MLDouble mlSingle = (MLDouble) mlArray;
        result = new double[dimensions[0]][dimensions[1]];

        for (int i = 0; i < dimensions[0]; i++) {
            for (int j = 0; j < dimensions[1]; j++) {
                    // 列优先，注意转换
                    result[i][j] = mlSingle.get(i, j);
            }
        }
        return result;
    }


    /**
     * 将MLArray转换为Java数组
     * @param mlArray 三维
     * @return
     */
    public static double[][][] transferMLArray(MLArray mlArray) {
        int[] dimensions = mlArray.getDimensions();
        double[][][] result = null;
        if (mlArray.isSingle()) {
            System.out.println("mlArray.isSingle()");
            MLSingle mlSingle = (MLSingle) mlArray;
            result = new double[dimensions[0]][dimensions[1]][dimensions[2]];

            for (int i = 0; i < dimensions[0]; i++) {
                for (int j = 0; j < dimensions[1]; j++) {
                    for (int k = 0; k < dimensions[2]; k++) {
                        // 列优先，注意转换
                        result[i][j][k] = mlSingle.get(i, k * dimensions[1] + j);
                    }
                }
            }
        } else if (mlArray.isDouble()) {
            System.out.println("mlArray.isDouble()");
            MLDouble mlSingle = (MLDouble) mlArray;
                result = new double[dimensions[0]][dimensions[1]][dimensions[2]];

                for (int i = 0; i < dimensions[0]; i++) {
                    for (int j = 0; j < dimensions[1]; j++) {
                        for (int k = 0; k < dimensions[2]; k++) {
                            // 列优先，注意转换
                            result[i][j][k] = mlSingle.get(i, k * dimensions[1] + j);
                        }
                    }
                }

        }

        return result;
    }

}
