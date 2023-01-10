import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

public class SubDataSolution {

    private double[][][] Epochs = new double[30][4000][8]; //存放matLab中需要用到的建模原始数据
    private double[][] Labels = new double[1][30]; //存放matLab中需要用到的30组建模标签
    private int currentTag = 0; //记录当前的标签
    private int portNumber = 8; //通道数
    private int groupCount = 0; //组数
    private int step = 9; //一个包中的数据长度
    private int count = 0; //
    private double[][] group1 = new double[4000][8];
    private static SubDataSolution _instance;
    private SubDataSolution(){}
    public static SubDataSolution get_instance() {
        if (_instance == null){
            _instance = new SubDataSolution();
        }
        return _instance;
    }

    public void addDataEpochs(String hexData) {
        int[] subData = solution(hexData);
        int tag = subData[subData.length-1];
        Labels[0][groupCount] = tag;
        for (int i = 0; i < step; i ++){
              for (int j = 0; j < portNumber;j++){
                  if (count<4000){
                      group1[count][j] = subData[i*portNumber+j];
                  }
              }
              count++;
              if (count == 4000){
                  Epochs[groupCount] = group1;
                  groupCount ++;
                  count = 0;
                  if (groupCount == 30){
                      groupCount = 0;
                  }
              }
        }
    }

    public double[][][] getEpochs() {
        return Epochs;
    }

    public double[][] getLabels(){
        return Labels;
    }

    private int[] solution(String data){
//        String hexString = data;
//        String[] arr = hexString.split(" ");
        String[] arr1 = data.split("-");

        //记录原始数据值
        int[] dataArr = new int[arr1.length];
        //存放截取的数据
        int[] subData = new int[216];
        //存放截取后的三个字节的数据
        int[][] echartsData = new int[72][3];
        //存放3字节转4字节的数据
        int[] _3byteTo4byte = new int[73];


        for (int i = 0;i < arr1.length;i ++){
            dataArr[i] = Integer.valueOf(arr1[i],16); //16字节字符串转整型
        }
        //拿到标签位
        int tag = dataArr[arr1.length-3];

        System.arraycopy(dataArr,2,subData,0,216);
        for (int i = 0; i < echartsData.length;i++){
            System.arraycopy(subData,i*3,echartsData[i],0,3);
        }
        //三字节转四字节整型
        for (int i = 0;i < echartsData.length;i++){
            _3byteTo4byte[i] = byteToInt(echartsData[i]);
        }
        if (tag == 1){
            _3byteTo4byte[_3byteTo4byte.length-1] = 1;
        }else if (tag == 2){
            _3byteTo4byte[_3byteTo4byte.length-1] = 0;
        }

        return _3byteTo4byte;
    }

    //三字节转四字节整型
    private int byteToInt(int[] bytes){
        int DataInt = 0;
        for (int i = 0;i < bytes.length; i++){
            DataInt = (DataInt << 8)|bytes[i];
        }
        //Log.d("移位后的整型数据：", "byteToInt: "+DataInt);
        if ((DataInt & 0x00800000) == 0x00800000){
            DataInt |= 0xFF000000;
        }else {
            DataInt &= 0x00FFFFFF;
        }
        return DataInt;
    }

}
