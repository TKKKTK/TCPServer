import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

public class SubDataSolution {

    private double[][][] Epochs = null; //存放matLab中需要用到的建模原始数据
    private double[][] Labels = null; //存放matLab中需要用到的30组建模标签
    private int currentTag = 0; //记录当前的标签
    private int portNumber = 8; //通道数
    private int groupCount = 0; //组数
    private int currentTagIndex = 0; //当前标签位的数组下标
    private int step = 9; //一个包中的数据长度
    private int count = 0; //一组数据中的下标
    private double[][][] trainData = null;
    private boolean isEnd = false; //判断训练数据是否截取满
    private boolean isStartSub = false;//判断是否开始截取数据

    public SubDataSolution(){}

    /**
     * 添加建模时的数据
     * @param hexData
     */
    public boolean addDataEpochs(String hexData) {
        int[] subData = solution(hexData);
        int tag = subData[subData.length-1];
        //在握拳/放松开始后的所有数据都加了一个标签
//        //添加标签数据
//         if (currentTag != tag){
//              if (tag == 0){
//                  if (currentTag == 1){
//                      Labels[0][currentTagIndex] = 1;
//                  }else if (currentTag == 2){
//                      Labels[0][currentTagIndex] = 0;
//                  }
//                  currentTagIndex++;
//                  groupCount++;
//                  count =0;
//              }
//             currentTag = tag;
//         }
//         //将打了标签的数据装进数组中
//         if (tag != 0){
//             for (int i = 0; i < step; i ++){
//                 for (int j = 0; j < portNumber;j++){
//                     if ( count < 4000){
//                         Epochs[groupCount][count][j] = subData[i*portNumber+j];
//                     }
//                 }
//                 count++;
//             }
//         }

        //只在握拳/放松后的第一个数据包中加一个标签
        //只在握拳/放松开始时加一个标签
        if (tag != 0){
            isStartSub = true;
            if (tag == 1){
                Labels[0][currentTagIndex] = 1;
            }else if (tag == 2){
                Labels[0][currentTagIndex] = 0;
            }
            currentTagIndex++;
        }

        if (isStartSub){
             for (int i = 0; i < step; i ++){
                 for (int j = 0; j < portNumber;j++){
                     if ( count < 4000){
                         Epochs[groupCount][count][j] = subData[i*portNumber+j];
                     }
                 }
                 count++;
                 if (count == 4000){
                     groupCount++;
                     isStartSub = false;
                     count = 0;
                     if (groupCount == 30){
                         groupCount = 0;
                         return true;
                     }
                 }
             }
        }
        return false;

    }

    /**
     * 添加训练时的数据
     * @param hexData
     * @return
     */
    public boolean addTrainData(String hexData){
        int[] subData = solution(hexData);
        int tag = subData[subData.length-1];
        //握拳/放松这段时间的数据我都加了标签
//        if (currentTag != tag){
//              if (tag == 0){
//                  isEnd = false;
//              }
//              currentTag = tag;
//        }
//        if (tag == 1 && !isEnd){
//            for (int i = 0; i < step; i ++){
//                for (int j = 0; j < portNumber;j++){
//                        trainData[0][count][j] = subData[i*portNumber+j];
//                }
//                count++;
//                if (count == 2500){
//                    isEnd = true;
//                    count = 0;
//                    return true;
//                }
//            }
//        }

        //只在握拳/放松开始时加一个标签
        if (tag != 0){
            isStartSub = true;
        }

        if (isStartSub){
            for (int i = 0; i < step; i ++){
                for (int j = 0; j < portNumber;j++){
                    trainData[groupCount][count][j] = subData[i*portNumber+j];
                }
                count++;
                if (count == 2500){
                    groupCount++;
                    isStartSub = false;
                    count = 0;
                    if (groupCount == 30){
                        groupCount = 0;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    //开始建模,初始化数据
    public void initData(){
        trainData = new double[30][2500][8];
        Epochs = new double[30][4000][8];
        Labels = new double[1][30];
        groupCount = 0;
        currentTagIndex = 0;
        currentTag = 0;
        count = 0;
    }

    public double[][][] getEpochs() {
        return Epochs;
    }

    public double[][] getLabels(){
        return Labels;
    }

    public double[][][] getTrainData() {
        return trainData;
    }

    public int[] solution(String data){
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
            try{
                dataArr[i] = Integer.valueOf(arr1[i].trim(),16); //16字节字符串转整型
            }catch (Exception e){
                e.printStackTrace();
            }

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

        _3byteTo4byte[_3byteTo4byte.length-1] = tag;

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
