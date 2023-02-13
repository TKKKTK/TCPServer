import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignExstrom;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilterTest {
    public static void main(String[] args) {
        FileReader fileReader = null;
        BufferedReader br = null;
        StringBuffer dataString = new StringBuffer();
         try {
             File file = new File("16_07_41_00.txt");
             fileReader = new FileReader(file);
             br = new BufferedReader(fileReader);
             String line = "";
             while ((line = br.readLine()) != null){
                 dataString.append(line);
             }
             fileReader.close();
             br.close();
         }catch (IOException e){
             e.printStackTrace();
         }

         String[] datas = dataString.toString().split("  ");
         //System.out.println(datas);
        //没滤波之前的原始数据
        List<List<Double>> originalList = new ArrayList<List<Double>>();
        for (int i = 0; i < 8; i++){
            originalList.add(new ArrayList<>());
        }

        SubDataSolution subDataSolution = new SubDataSolution();
        for (int i = 0; i < datas.length; i++){
               int[] subData = subDataSolution.solution(datas[i]);
               for (int j = 0; j < originalList.size(); j++){
                   List<Double> originals = originalList.get(j);
                   for (int k = 0; k < 9; k++){
                     originals.add((double) subData[k*8 + j]);
                   }
               }
        }

        //声明滤波器，进行限波滤波
        //存放滤完波后的数据
        double[][] filterData = new double[8][datas.length*9];
        IirFilterCoefficients iirFilterCoefficientsNotch = new IirFilterCoefficients();
        //限波滤波器
        double fs = 500;
        double r = 0.9;
        double w0 = (2 * Math.PI * 50 / fs);
//      f0=50;fs=500;r=0.9;
//      w0=2*pi*f0/fs;
//      b=[1 -2*cos(w0) 1];
//      a=[1 -2*r*cos(w0) r*r];
        //限波滤波中自定义的a,b系数
        double[] b0 = new double[]{1,-2*Math.cos(w0),1};
        double[] a0 = new double[]{1,-2*r*Math.cos(w0),r*r};
        iirFilterCoefficientsNotch.a = a0;
        iirFilterCoefficientsNotch.b = b0;



        //低通滤波器
        IirFilterCoefficients iirFilterCoefficientsLowPass = IirFilterDesignExstrom.design(FilterPassType.lowpass,2,30.0/500,0);

        //低通滤波
        List<IirFilter> iirFilterLowPass = new ArrayList<>();
        for (int i = 0; i < originalList.size();i++){
            iirFilterLowPass.add(new IirFilter(iirFilterCoefficientsLowPass));
            IirFilter iirFilter = iirFilterLowPass.get(i);
            List<Double> originals = originalList.get(i);
            for (int j = 0; j < originals.size(); j++){
                filterData[i][j] = iirFilter.step(originals.get(j));
            }
        }

        //高通滤波器 IirFilterDesignExstrom.design(filterPassType,i,v,v1); filter.initFilter(FilterPassType.highpass,4,mFrequency/500,0)
        IirFilterCoefficients iirFilterCoefficients = IirFilterDesignExstrom.design(FilterPassType.highpass,4,1.0/500,0);

        //高通滤波
        List<IirFilter> iirFilters = new ArrayList<>();
        for (int i = 0; i < filterData.length; i++){
            iirFilters.add(new IirFilter(iirFilterCoefficients));
            IirFilter iirFilter = iirFilters.get(i);
            for (int j = 0; j < filterData[i].length; j++){
                 filterData[i][j] = iirFilter.step(filterData[i][j]);
            }
        }
        //单独高通滤波
//        List<IirFilter> iirFilters = new ArrayList<>();
//        for (int i = 0; i < originalList.size();i++){
//            iirFilters.add(new IirFilter(iirFilterCoefficients));
//            IirFilter iirFilter = iirFilters.get(i);
//            List<Double> originals = originalList.get(i);
//            for (int j = 0; j < originals.size(); j++){
//                filterData[i][j] = iirFilter.step(originals.get(j));
//            }
//        }

        //陷波滤波
        List<IirFilter> iirFiltersNotch = new ArrayList<>();
        for (int i = 0; i < filterData.length; i++){
            iirFiltersNotch.add(new IirFilter(iirFilterCoefficientsNotch));
            IirFilter iirFilter = iirFiltersNotch.get(i);
            for (int j = 0; j < filterData[i].length; j++){
                filterData[i][j] = iirFilter.step(filterData[i][j]);
            }
        }
          //单独陷波滤波
//        List<IirFilter> iirFiltersNotch = new ArrayList<>();
//        for (int i = 0; i < originalList.size();i++){
//            iirFiltersNotch.add(new IirFilter(iirFilterCoefficientsNotch));
//            IirFilter iirFilter = iirFiltersNotch.get(i);
//            List<Double> originals = originalList.get(i);
//            for (int j = 0; j < originals.size(); j++){
//                filterData[i][j] = iirFilter.step(originals.get(j));
//            }
//        }

        //将滤完波的数据存放到矩阵中，提供给matlab做数据分析
        File file = new File("JavaFilterData.mat");
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        MLDouble mlDouble = new MLDouble("JavaFilterData",filterData);
        list.add(mlDouble);
        try {
            new MatFileWriter(file,list);
        } catch (IOException e) {
            e.printStackTrace();
        }

        double[][] originalData = new double[8][datas.length*9];
        //将没滤波的数据存放到矩阵中，提供给matlab做数据分析
        for (int i = 0; i < originalList.size(); i++){
            for (int j = 0; j < originalList.get(i).size();j++){
                originalData[i][j] = originalList.get(i).get(j);
            }
        }
        File file1 = new File("JavaData.mat");
        ArrayList<MLArray> list1 = new ArrayList<MLArray>();
        MLDouble mlDouble1 = new MLDouble("JavaData",originalData);
        list1.add(mlDouble1);
        try {
            new MatFileWriter(file1,list1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
