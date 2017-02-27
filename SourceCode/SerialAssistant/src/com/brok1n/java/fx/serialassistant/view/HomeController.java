package com.brok1n.java.fx.serialassistant.view;

import com.brok1n.java.fx.serialassistant.AppDelegate;
import com.brok1n.java.fx.serialassistant.utils.Butils;
import gnu.io.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 * Created by brok1n on 2017/2/16.
 */
public class HomeController implements SerialPortEventListener {

    //串口连接超时时间
    public static final int CONNECT_SERIAL_TIMEOUT = 2000;

    //当前串口列表
    static Enumeration<CommPortIdentifier> portList;

    private SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //串口列表
    @FXML
    private ComboBox serialComboBox;

    //波特率列表
    @FXML
    private ComboBox baudRateComboBox;

    //自定义波特率复选框
    @FXML
    private CheckBox customBaudRateCheckbox;

    //自定义波特率输入框
    @FXML
    private TextField customBaudRateTextField;

    //数据位列表
    @FXML
    private ComboBox dataBitComboBox;

    //校验位列表
    @FXML
    private ComboBox checkBitComboBox;

    //停止位列表
    @FXML
    private ComboBox stopBitComboBox;

    //状态LED
    @FXML
    private Circle statusLed;

    //打开、关闭 按钮
    @FXML
    private Button openCloseBtn;

    //状态栏 状态Label
    @FXML
    private Label statusLabel;

    //数据窗口
    @FXML
    private TextArea dataTextArea;

    //接收数据总数label
    @FXML
    private Label statusReceivedLabel;

    //发送数据总数label
    @FXML
    private Label statusSendLabel;

    //串口刷新文本按钮
    @FXML
    private Label refreshSerialListLabel;

    //接收转文件复选框
    @FXML
    private CheckBox receivedToFileCbox;

    //显示接收时间复选框
    @FXML
    private CheckBox showReceivedCbox;

    //显示十六进制复选框
    @FXML
    private CheckBox showHexCbox;

    //保存数据按钮
    @FXML
    private Button saveReceivedDataBtn;

    //停止接收显示复选框
    @FXML
    private CheckBox stopShowReceivedDataCbox;

    //接收数据存储到的文件
    private File receivedToFile;


    //串口是否被打开
    private boolean serialIsOpened;

    //当前连接的串口
    CommPortIdentifier currentSerial;

    //当前串口连接对象
    SerialPort currentSerialPort;

    //串口输入流
    InputStream currentSerialInputStream;

    //串口输出流
    OutputStream currentSerialOutputStream;

    //波特率 默认9600
    private int baudRate = 9600;

    //数据位 默认8
    private int dataBit = SerialPort.DATABITS_8;

    //校验位 默认无
    private int checkBit = SerialPort.PARITY_NONE;

    //停止位 默认1 1、1.5、2
    private int stopBit = SerialPort.STOPBITS_1;

    //串口发送数据总数
    private int serialSendDataCount;

    //串口接收数据总数
    private int serialReceivedDataCount;

    //是否显示接收时间
    private boolean showReceivedTime = false;

    //读取到的数据
    public String readStr;

    /**
     * 预分配1M空间给byte缓存
     * */
    ByteBuffer byteBuffer = ByteBuffer.allocate( 1024 * 1024 );
    byte[] tmpByteArr = new byte[1024];


    public void init()
    {
        System.out.println("home界面初始化");

        new Thread(new SerialRunnable()).start();

    }

    class SerialRunnable implements Runnable
    {

        @Override
        public void run() {
            portList = CommPortIdentifier.getPortIdentifiers();

            System.out.println("串口:" + portList.toString());

            //初始化界面
            initView();

        }

        /**
         * 初始化界面
         * */
        public void initView()
        {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    //初始化串口列表
                    initSerialList();
                    serialComboBox.setVisibleRowCount(5);

                    //初始化波特率列表
                    baudRateComboBox.getItems().addAll("300", "600", "1200", "2400", "4800", "9600", "19200", "38400", "43000", "56000", "57600", "115200");
                    baudRateComboBox.setValue("9600");
                    baudRateComboBox.setVisibleRowCount(5);

                    //初始化禁用自定义波特率输入框
                    customBaudRateTextField.setDisable(true);

                    //初始化数据位列表
                    dataBitComboBox.getItems().addAll("5", "6", "7", "8");
                    dataBitComboBox.setValue("8");

                    /**
                     *  even 每个字节传送整个过程中bit为1的个数是偶数个（校验位调整个数）
                     2. odd 每个字节穿送整个过程中bit为1的个数是奇数个（校验位调整个数）
                     3. noparity没有校验位
                     4. space 校验位总为0
                     5. mark 校验位总为1
                     PARITY_NONE
                     PARITY_ODD
                     PARITY_EVEN
                     PARITY_MARK
                     PARITY_SPACE
                     * */
                    //初始化校验位列表
                    checkBitComboBox.getItems().addAll("none", "odd", "even", "mark", "space");
                    checkBitComboBox.setValue("none");

                    //初始化停止位列表
                    stopBitComboBox.getItems().addAll("1", "1.5", "2");
                    stopBitComboBox.setValue("1");

                    //关闭串口
                    closeSerial();

                }
            });
        }
    }

    /**
     * 初始化串口列表
     * */
    public void initSerialList()
    {
        if( portList == null || serialComboBox == null )
        {
            System.out.println("串口列表初始化失败！");
            return ;
        }

        serialComboBox.getItems().clear();
        while ( portList.hasMoreElements() )
        {
            CommPortIdentifier obj = (CommPortIdentifier) portList.nextElement();
            System.out.println(":" + obj.getName() );
            serialComboBox.getItems().add( obj.getName() );
        }
    }

    /**
     * 打开/关闭 按钮被点击
     * */
    @FXML
    public void onOpenCloseBtnClicked()
    {
        System.out.println("打开按钮被点击");
        if( serialIsOpened )
        {
            closeSerial();
        }
        else
        {
            openSerial();
        }
    }

    /**
     * 关闭串口
     * */
    public synchronized void closeSerial()
    {
        Butils.log("关闭串口");
        serialIsOpened = false;

        if( currentSerialPort != null )
        {
            currentSerialPort.notifyOnDataAvailable(false);

            currentSerialPort.close();
            currentSerialPort = null;
            currentSerialInputStream = null;
            currentSerialOutputStream = null;
        }


        statusLed.setStyle("-fx-fill: #FF0000; -fx-stroke: #FF0000");
        openCloseBtn.setText("打开串口");

        //可以修改当前串口参数
        serialComboBox.setDisable(false);
        refreshSerialListLabel.setDisable(false);
        baudRateComboBox.setDisable(customBaudRateCheckbox.isSelected());
        customBaudRateCheckbox.setDisable(false);
        customBaudRateTextField.setDisable(!customBaudRateCheckbox.isSelected());
        dataBitComboBox.setDisable(false);
        checkBitComboBox.setDisable(false);
        stopBitComboBox.setDisable(false);

    }

    /**
     * 打开串口
     * */
    public void openSerial()
    {
        //获取当前选择的串口设备
        if( serialComboBox.getValue() == null )
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("请选择要连接的串口");

            alert.showAndWait();
            return ;
        }
        String selectSerialName = serialComboBox.getValue().toString();
        portList = CommPortIdentifier.getPortIdentifiers();
        while ( portList.hasMoreElements() )
        {
            CommPortIdentifier cpi = portList.nextElement();
            if( cpi != null && cpi.getName().equals(selectSerialName) && cpi.getPortType() == CommPortIdentifier.PORT_SERIAL )
            {
                currentSerial = cpi;
                break;
            }
        }

        //初始化串口参数
        baudRate = getBaudRate();
        dataBit  = getDataBit();
        checkBit = getCheckBit();
        stopBit  = getStopBit();

        //连接串口
        if( connectSerial() )
        {
            serialIsOpened = true;

            //修改状态
            statusLed.setStyle("-fx-fill: #00FF00; -fx-stroke: #00FF00");
            openCloseBtn.setText("关闭串口");

            //连接成功禁止修改当前串口参数
            serialComboBox.setDisable(true);
            refreshSerialListLabel.setDisable(true);
            baudRateComboBox.setDisable(true);
            customBaudRateCheckbox.setDisable(true);
            customBaudRateTextField.setDisable(true);
            dataBitComboBox.setDisable(true);
            checkBitComboBox.setDisable(true);
            stopBitComboBox.setDisable(true);
        }
        else
        {
            closeSerial();
        }

    }

    /**
     * 建立串口连接
     * */
    public boolean connectSerial()
    {
        //串口是否建立成功
        boolean status = false;
        try {
            //建立串口连接、设置参数、监听器
            currentSerialPort = (SerialPort) currentSerial.open(currentSerial.getName(), HomeController.CONNECT_SERIAL_TIMEOUT);
            currentSerialPort.addEventListener(this);
            currentSerialPort.notifyOnDataAvailable(true);
                        /* 设置串口通讯参数 */
            currentSerialPort.setSerialPortParams( baudRate, dataBit, stopBit, checkBit);

            //串口输入输出流
            currentSerialInputStream = currentSerialPort.getInputStream();
            currentSerialOutputStream = currentSerialPort.getOutputStream();

            status = true;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String name = currentSerial.getName();
                    name = name.indexOf(File.separator) != -1 ? name.substring(name.lastIndexOf(File.separator) + 1) : name;
                    statusLabel.setText(name + " " + baudRate + " " + dataBitComboBox.getValue().toString() + " " + checkBitComboBox.getValue().toString() + " " + stopBitComboBox.getValue().toString() );
                    statusLabel.setStyle("-fx-text-fill: #000000");
                }
            });

        } catch (TooManyListenersException e) {
            e.printStackTrace();
            status =false;
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
            status = false;
        } catch (PortInUseException e) {
            e.printStackTrace();
            status = false;
        } catch (IOException e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    /**
     * 获取波特率
     * */
    public int getBaudRate()
    {
       return customBaudRateCheckbox.isSelected() ? Integer.parseInt(customBaudRateTextField.getText()) : Integer.parseInt(baudRateComboBox.getValue().toString());
    }

    /**
     * 获取数据位参数
     * */
    public int getDataBit()
    {
        return Integer.parseInt( dataBitComboBox.getValue().toString() );
    }

    /**
     * 获取当前的校验位
     * PARITY_NONE
     * PARITY_ODD
     * PARITY_EVEN
     * PARITY_MARK
     * PARITY_SPACE
     * */
    public int getCheckBit()
    {
        int index = checkBitComboBox.getSelectionModel().getSelectedIndex();
        switch (index)
        {
            default:
            case 0:
                index = SerialPort.PARITY_NONE;
                break;
            case 1:
                index = SerialPort.PARITY_ODD;
                break;
            case 2:
                index = SerialPort.PARITY_EVEN;
                break;
            case 3:
                index = SerialPort.PARITY_MARK;
                break;
            case 4:
                index = SerialPort.PARITY_SPACE;
                break;
        }
        return index;
    }

    /**
     * 获取停止位
     * */
    public int getStopBit()
    {
        int index = stopBitComboBox.getSelectionModel().getSelectedIndex();
        switch (index)
        {
            default:
            case 0:
                index = SerialPort.STOPBITS_1;
                break;
            case 1:
                index = SerialPort.STOPBITS_1_5;
                break;
            case 2:
                index = SerialPort.STOPBITS_2;
                break;
        }
        return index;
    }

    /**
     * 刷新串口
     * */
    @FXML
    public void onRefreshLabelClicked()
    {
        //串口列表被点击 重新获取串口列表
        portList = CommPortIdentifier.getPortIdentifiers();
        initSerialList();
    }

    /**
     * 自定义波特率复选框被选中
     * */
    @FXML
    public void onCustomBaudRateCboxClicked()
    {
        customBaudRateTextField.setDisable(!customBaudRateCheckbox.isSelected());
        baudRateComboBox.setDisable(customBaudRateCheckbox.isSelected());
    }

    /**
     * 清除显示按钮被点击
     * */
    @FXML
    public void onClearTextAreaBtnClicked()
    {
        dataTextArea.setText("");
    }

    /**
     * 发送接收统计清除按钮被点击
     * */
    @FXML
    public void onClearRSCountBtnClicked()
    {
        serialReceivedDataCount = 0;
        serialSendDataCount = 0;
        statusReceivedLabel.setText("接收:" + serialReceivedDataCount );
        statusSendLabel.setText("发送:" + serialSendDataCount );
    }

    /**
     * 接收转文件复选框被点击
     * */
    @FXML
    public void onReceivedToFileCboxClicked()
    {
        if( !receivedToFileCbox.isSelected() )
        {
            receivedToFileCbox.setSelected(false);
            dataTextArea.setText("");

            readStr = "";
            byteBuffer.clear();

            receivedToFile = null;
            return ;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showSaveDialog(AppDelegate.static_stage);

        if( file == null )
        {
            receivedToFileCbox.setSelected(false);
            return;
        }

        boolean canCreate = true;
        if(!file.exists())
        {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                canCreate = true;
            } catch (IOException e) {
                e.printStackTrace();
                canCreate = false;
            }
        }

        if( !canCreate || !file.canRead() || !file.canWrite() )
        {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("抱歉！您选择的文件无法读取或写入，请选择其他文件或使用管理员权限运行本程序。");

            alert.showAndWait();

            receivedToFileCbox.setSelected(false);

            return ;
        }

        dataTextArea.clear();
        dataTextArea.setText("串口接收数据转向到文件：" + file.getAbsolutePath() );

        readStr = "";
        byteBuffer.clear();

        receivedToFile = file;

        Butils.log( "选择了:" + file.getAbsolutePath() );

    }

    /**
     * 是否显示接收时间被点击
     * */
    @FXML
    public void onShowReceivedTimeCboxClicked()
    {
        if( showReceivedCbox.isSelected() )
        {
            showReceivedTime = true;
        }
        else
        {
            showReceivedTime = false;
        }
    }

    /**
     * 显示十六进制复选框被点击
     * */
    @FXML
    public void onShowHexCboxClicked()
    {
        if( showHexCbox.isSelected() )
        {
            dataTextArea.setText( Butils.string2HexString( dataTextArea.getText() ) );
            //dataTextArea.selectPositionCaret( dataTextArea.getText().length());
            dataTextArea.positionCaret(dataTextArea.getText().length());
        }
        else
        {
            dataTextArea.setText( Butils.hexString2String( dataTextArea.getText() ) );
            //dataTextArea.selectPositionCaret( dataTextArea.getText().length());
            dataTextArea.positionCaret(dataTextArea.getText().length());
        }
    }




    @Override
    public void serialEvent(SerialPortEvent event) {

        switch (event.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE://获取到串口返回信息

                receivedData();

                byte[] readBuffer = new byte[1024];
                readStr= showReceivedTime ? "[" + sd.format(new Date()) + "]" : "";
                try {
                    while (currentSerialInputStream.available() > 0)
                    {
                        int len = currentSerialInputStream.read(readBuffer);
                        byteBuffer.put(readBuffer, 0, len);
                        if( showHexCbox.isSelected() )
                        {
                            readStr += Butils.bytes2HexString( readBuffer, 0, len );
                        }
                        else
                        {
                            readStr += new String(readBuffer);
                        }
                        serialReceivedDataCount += len;
                    }

                    Butils.log("接收到端口返回数据(长度为"+readStr.length()+")："+readStr);
                    Butils.log( Thread.currentThread().getName() );

                    Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                         double d= dataTextArea.scrollTopProperty().getValue();
                         if( showHexCbox.isSelected() )
                         {
                             dataTextArea.appendText( Butils.string2HexString(readStr) );
                         }
                         else
                         {
                             dataTextArea.appendText( readStr );
                         }

                         int point = dataTextArea.getCaretPosition();
                         if( point > 4000 )
                         {
                             dataTextArea.setText( dataTextArea.getText().substring(point - 4000 ));
                             //dataTextArea.selectPositionCaret( dataTextArea.getText().length());
                             dataTextArea.positionCaret(dataTextArea.getText().length());
                         }
                         statusReceivedLabel.setText("接收:" + serialReceivedDataCount );
                         Butils.log("scrollTop:" + d + "  receiveSize:" + dataTextArea.getText().length() + " point:" + dataTextArea.getCaretPosition());
                     }
                    });

                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                break;
            default:
                break;
        }
    }

    /**
     * 接收数据
     * */
    private void receivedData()
    {
        try {
            int len = 0;
            while ( currentSerialInputStream.available() > 0 )
            {
                len = currentSerialInputStream.read( tmpByteArr );
                byteBuffer.put( tmpByteArr, 0, len );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出接收
     * */
    private void receivedOutput(byte[] data )
    {

    }

}
