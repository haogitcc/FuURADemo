package com.thingmagic;

import static com.thingmagic.TMConstants.TMR_PARAM_REGION_HOPTABLE;
import static com.thingmagic.TMConstants.TMR_PARAM_REGION_HOPTIME;
import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator; 
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;

public class MainController implements Initializable {

    public Stage stage;
    @FXML
    private TabPane mainTabs;
    @FXML
    private Tab homeTab;
    @FXML
    private Tab connectTab;
    @FXML
    private Tab configureTab;
    @FXML
    private Tab readTab;
    @FXML
    private Tab helpTab;
    
    //Home Tab
    @FXML
    private BorderPane homeBorderPane;

    //Connect Tab Controllers
    @FXML
    private ImageView reloadDevices;
    @FXML
    private Button reloadDevicesButton;
    @FXML
    private VBox readerList;
    @FXML
    private Pane suggestionPane;
    @FXML
    private ProgressIndicator reloadDevicesProgress;
    @FXML
    private VBox readerProperties;

    @FXML
    private ComboBox probeBaudRate;
    @FXML
    private Button connectionButton;
    @FXML
    private ProgressIndicator connectProgressIndicator;

    //Configure Tab Controllers
    @FXML
    private Accordion accordion;
    @FXML
    private TitledPane readWriteTitledPane;
    @FXML
    private TitledPane performanceTuningTitlePane;
    @FXML
    private TitledPane regulatoryTestingPane;
    @FXML
    private TitledPane displayOptionsTitlePane;
    @FXML
    private TitledPane profileTitlePane;
    @FXML
    private TitledPane firmwareUpdateTitledPane;
    @FXML
    private TitledPane aboutTitledPane;
    @FXML
    private ComboBox region;
    @FXML
    private CheckBox gen2;
    @FXML
    private CheckBox iso18000;
    @FXML
    private CheckBox ipx64;
    @FXML
    private CheckBox ipx256;
    @FXML
    private CheckBox cbTransportLogging;
    @FXML
    private CheckBox antenna1;
    @FXML
    private CheckBox antenna2;
    @FXML
    private CheckBox antenna3;
    @FXML
    private CheckBox antenna4;
    @FXML
    private CheckBox antennaDetection;

    @FXML
    private CheckBox quickSearch;
    @FXML
    private RadioButton dynamic;
    @FXML
    private RadioButton equalTime;

    @FXML
    private TextField dutyCycleOn;
    @FXML
    private TextField dutyCycleOff;
    @FXML
    private Text dutyCycleOnText;
    @FXML
    private Text dutyCycleOffText;

    @FXML
    private ComboBox epcStreamFormat;

    @FXML
    private CheckBox gpiTriggerRead;

    @FXML
    private CheckBox autonomousRead;

    @FXML
    private RadioButton autoReadGpi1;
    @FXML
    private RadioButton autoReadGpi2;
    @FXML
    private RadioButton autoReadGpi3;
    @FXML
    private RadioButton autoReadGpi4;

    @FXML
    private CheckBox metaDataEpc;
    @FXML
    private CheckBox metaDataTimeStamp;
    @FXML
    private CheckBox metaDataRssi;
    @FXML
    private CheckBox metaDataReadCount;   
    
    @FXML
    private CheckBox metaDataAntenna;
    @FXML
    private CheckBox metaDataProtocol;    
    @FXML
    private CheckBox metaDataFrequency;   
    @FXML
    private CheckBox metaDataPhase;    
    
    @FXML
    private CheckBox embeddedReadEnable;
    @FXML
    private CheckBox embeddedReadUnique;
    @FXML
    private ComboBox embeddedMemoryBank;
    @FXML
    private TextField embeddedStart;
    @FXML
    private TextField embeddedEnd;

    @FXML
    private TextField rfRead;
    @FXML
    private TextField rfWrite;

    @FXML
    private RadioButton link640Khz;
    @FXML
    private RadioButton link250Khz;

    @FXML
    private RadioButton tari25us;
    @FXML
    private RadioButton tari12_5us;
    @FXML
    private RadioButton tari6_25us;

    @FXML
    private RadioButton fm0;
    @FXML
    private RadioButton m2;
    @FXML
    private RadioButton m4;
    @FXML
    private RadioButton m8;

    @FXML
    private RadioButton sessionS0;
    @FXML
    private RadioButton sessionS1;
    @FXML
    private RadioButton sessionS2;
    @FXML
    private RadioButton sessionS3;

    @FXML
    private RadioButton targetA;
    @FXML
    private RadioButton targetB;
    @FXML
    private RadioButton targetAB;
    @FXML
    private RadioButton targetBA;

    @FXML
    private RadioButton dynamicQ;
    @FXML
    private RadioButton staticQ;
    @FXML
    private ComboBox staticQList;

    @FXML
    private Label lRfidEngine;
    @FXML
    private Label lFirmwareVersion;
    @FXML
    private Label lHardwareVersion;
    @FXML
    private Label lActVersion;
    @FXML
    private Label lMercuryApiVersion;

    @FXML
    private Label minPower;
    @FXML
    private Label minPower1;
    @FXML
    private Label maxPower;
    @FXML
    private Label maxPower1;

    @FXML
    private Slider readPowerSlider;
    @FXML
    private Slider writePowerSlider;
    @FXML
    private CheckBox powerEquator;

    @FXML
    private ToggleGroup linkFreqGroup;
    @FXML
    private ToggleGroup tariGroup;
    @FXML
    private ToggleGroup tagEncodeGroup;
    @FXML
    private ToggleGroup targetGroup;
    @FXML
    private ToggleGroup sessionGroup;
    @FXML
    private ToggleGroup qGroup;
    @FXML
    private ToggleGroup autoReadGpiGroup;
    @FXML
    private Button applyButton;
    @FXML
    private Button revertButton;
    @FXML
    private Button loadConfigButton;
    @FXML
    private Button saveConfigButton;
    @FXML
    private VBox changeListContainer;
    @FXML
    private HBox regionParentNode;
    private String firmwareFile ;
    private FileInputStream fi;
    private boolean isReading = false;
    @FXML
    private Label keepAliveTimeLabel;
    @FXML
    private TextField keepAliveTime;
    
    @FXML
    private TextArea hopTable;
    
    @FXML
    private TextField hopTime;

    private boolean isLoadSaveConfiguration = false;

    //Read Tab
    ConcurrentHashMap<String, TagReadData> tagData = new ConcurrentHashMap<String, TagReadData>();
    @FXML
    private TableView tableView;
    @FXML
    private TableColumn deviceIdColumn;
    @FXML
    private TableColumn dataColumn;
    @FXML
    private TableColumn epcColumn;
    @FXML
    private TableColumn timeStampColumn;
    @FXML
    private TableColumn rssiColumn;
    @FXML
    private TableColumn countColumn;    
    @FXML
    private TableColumn antennaColumn;
    @FXML
    private TableColumn protocolColumn;
    @FXML
    private TableColumn frequencyColumn;
    @FXML
    private TableColumn phaseColumn;
    @FXML
    private TextField selectedFilePath;
    @FXML
    private Button loadFirmware;
    @FXML
    private Button updateFirmware;
    @FXML
    private Button btGetStarted;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label temperature;
    
    @FXML
    private BorderPane popupMsgBorderPane;
    @FXML
    private Pane popupMsgPane;
    @FXML
    private ImageView popupCloseImg;
    @FXML
    private ImageView warningDownArrow;
    
    @FXML
    private ImageView successDownArrow;
    
    @FXML
    private ImageView errorDownArrow;
    
    @FXML
    private Text popupMsgContentLabel;
    @FXML
    private Text  popupMsgTitle;
    
    //Help Tab
    @FXML
    private BorderPane helpBorderPane;

    //Footer Controllers
    @FXML
    private Circle connectStatus;
    @FXML
    private Label statusLabel;
    
    //Connect Tab Objects 
    Reader r = null;
    private boolean isConnected = false;
    HashMap<String,HashMap> comportInfo = new HashMap<String,HashMap>();

    //private Text comportName;
    private String deviceName;
    
    //Read Tab    
    ReadListener readListener;
    ReadExceptionListener exceptionListener;
    StatsListener statsListener;
    int cloumnCount=4;
    long totalTag;
    int uniqueTag;
    @FXML 
    private Text uniqueTagCount;
    @FXML 
    private Text totalTagCount;
    ObservableList<TagResults> row;
    private boolean isAutonomousReadStarted = false;

    //Enable or Disable Opacity
    double buttonEnableOpacity = 1.0;
    double enableOpacity = 0.7;
    double disableOpacity = 0.09;
      
    Task progressTask;
    FileChooser fileChooser;
    Map<String, String> saveParams = new HashMap<String, String>();
    LoadSaveConfig loadSave ;
    List<String> fileFilters;
    int minReaderPower = 0;
    int maxReaderPower = 0;
    List<Integer> existingAntennas;
    List<String> supportedProtocols;
    String hardWareVersion, readerModel = "", firmwareVerson;

    //configurations change massage variables for change list
    boolean showChangeList = false;
    HashMap<String, String> changeListMap = new HashMap<String, String>();
    TextField customComportField;
    boolean isAutonomousSupported = false, isTransportLogsEnabled = false;
    String connectedDevice;
    List<String> comportList = new ArrayList<String>();
    
    int statsTemperature = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    SimpleDateFormat dfhms = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    TransportListener transportListener;
    BufferedWriter transportWriter;
    Reader.Region currentRegion = Reader.Region.UNSPEC;
    TagProtocol currentProtocol = null;
    StringBuffer loadSaveError = null;
    Properties loadSaveProperties;
    volatile boolean isLoadSaveCompleted = false;
    boolean isRegionChanged = true;

    
    @Override
    public void initialize(URL location, ResourceBundle resources) 
    {
        new Thread(findReadersThread).start();
        mainTabListener();
        disableFeatures();
        powerSlider();
        validateTextFields();
        checkBoxListener();

        //Setting ToolTip
        setTooltip();
        //Addind and deleting rows based on selection
        tableViewConfiguration();

        ObservableList<String> row = FXCollections.observableArrayList();

        probeBaudRate.getSelectionModel().select("115200");

        setNoColoumsInTable(cloumnCount);

        //Select Frist TilledPane in Accorion
        ObservableList<TitledPane> list = accordion.getPanes();
        accordion.setExpandedPane(list.get(0));
        applyButton.setDisable(true);
        applyButton.setOpacity(buttonEnableOpacity);
        revertButton.setDisable(true);
        revertButton.setOpacity(buttonEnableOpacity);
        setHomeContent();
        showAboutInfo();
    }
    
    public void disableFeatures()
    {     
        quickSearch.setDisable(true);
        quickSearch.setOpacity(enableOpacity);

        dutyCycleOff.setDisable(false);
        dutyCycleOff.setOpacity(enableOpacity);
        dutyCycleOn.setDisable(false);
        dutyCycleOn.setOpacity(enableOpacity);

        dutyCycleOffText.setOpacity(buttonEnableOpacity);
        dutyCycleOnText.setOpacity(buttonEnableOpacity);
        
        dutyCycleOff.setText("0");
        dutyCycleOn.setText("1000");

        epcStreamFormat.setDisable(true);
        epcStreamFormat.setOpacity(disableOpacity);

        dynamic.setDisable(true);
        dynamic.setOpacity(disableOpacity);
        equalTime.setOpacity(disableOpacity);
        equalTime.setDisable(true);
        connectProgressIndicator.visibleProperty().setValue(false);
        
        keepAliveTime.setVisible(false);
        keepAliveTimeLabel.setVisible(false);
    }

    public void setStage(Stage stage) 
    {
        this.stage = stage;
    }

    public void mainTabListener() 
    {
        ImageView imv = new ImageView(new Image("images/home_active.png"));
        homeTab.setGraphic(imv);
        imv = new ImageView(new Image("images/connect.png"));
        connectTab.setGraphic(imv);
        imv = new ImageView(new Image("images/config.png"));
        configureTab.setGraphic(imv);
        imv = new ImageView(new Image("images/read.png"));
        readTab.setGraphic(imv);
        imv = new ImageView(new Image("images/help.png"));
        helpTab.setGraphic(imv);

        mainTabs.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() 
                {
                    public void changed(ObservableValue<? extends Tab> ov, Tab previousTab, Tab newTab) 
                    {
                        
                        if (previousTab.getText().equalsIgnoreCase("home")) 
                        {
                            ImageView imv = new ImageView(new Image("images/home.png"));
                            homeTab.setGraphic(imv);
                        } 
                        else if (previousTab.getText().equalsIgnoreCase("connect")) 
                        {
                            ImageView imv = new ImageView(new Image("images/connect.png"));
                            connectTab.setGraphic(imv);
                        }
                        else if (previousTab.getText().equalsIgnoreCase("configure")) 
                        {
                            ImageView imv = new ImageView(new Image("images/config.png"));
                            configureTab.setGraphic(imv);
                        }
                        else if (previousTab.getText().equalsIgnoreCase("help")) 
                        {
                            ImageView imv = new ImageView(new Image("images/help.png"));
                            helpTab.setGraphic(imv);
                        } else if (previousTab.getText().equalsIgnoreCase("read")) 
                        {
                            ImageView imv = new ImageView(new Image("images/read.png"));
                            readTab.setGraphic(imv);
                        }

                        if (newTab.getText().equalsIgnoreCase("home")) 
                        {
                            ImageView imv = new ImageView(new Image("images/home_active.png"));
                            homeTab.setGraphic(imv);
                        }
                        else if (newTab.getText().equalsIgnoreCase("connect")) 
                        {   
                            ImageView imv = new ImageView(new Image("images/connect_active.png"));
                            connectTab.setGraphic(imv);
                        } 
                        else if (newTab.getText().equalsIgnoreCase("configure")) 
                        {
                            ImageView imv = new ImageView(new Image("images/config_active.png"));
                            configureTab.setGraphic(imv);
                            if (!isConnected)
                            {
                                mainTabs.getSelectionModel().select(connectTab);
                                showWarningErrorMessage("warning","Please connect reader to configure");
                            }
//                            else if(isAutonomousReadStarted)
//                            {
//                                showWarningErrorMessage("warning","Please disconnect and connect back to configure the reader");
//                            }
                        }
                        else if (newTab.getText().equalsIgnoreCase("help")) 
                        {
                            ImageView imv = new ImageView(new Image("images/help_active.png"));
                            helpTab.setGraphic(imv);
                        }
                        else if (newTab.getText().equalsIgnoreCase("read")) 
                        {
                            ImageView imv = new ImageView(new Image("images/read_active.png"));
                            readTab.setGraphic(imv);
                        }
                    }
                });
    }

    //Realod Devices list on  mouse released
    @FXML
    private void reloadDevices()
    {
        readerList.getChildren().clear();
        readerProperties.setVisible(false);
        reloadDevicesButton.setDisable(true);
        new Thread(findReadersThread).start();
    }

    //show list of readers
    private Runnable findReadersThread = new Runnable()
    {

        @Override
        public void run()
        {            
            Platform.runLater(new Runnable()
           {
               @Override
               public void run()
               {
                   reloadDevicesProgress.visibleProperty().setValue(true);
                   progressTask = createProgress();
                   reloadDevicesProgress.progressProperty().unbind();
                   reloadDevicesProgress.progressProperty().bind(progressTask.progressProperty());
               }
               
           });
           try
           {
            comportInfo.clear();
            comportList.clear();
//            Enumeration portList = CommPortIdentifier.getPortIdentifiers();
//            while (portList.hasMoreElements())
//            {
//                final CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
//                if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL)
//                {
//                    if(!(portId.getName().contains("Bluetooth") ||  portId.getName().contains("tty.usbserial")))
//                    {
//                        setReaders(portId.getName());
//                        comportList.add(portId.getName());
//                    }
//                }
//            }
            SerialPort[] portList;
            String[] results;
            // get comm ports
            portList = SerialPort.getCommPorts();
            results = new String[portList.length];
            for (int i = 0; i < portList.length; i++) 
            {
                results[i] = portList[i].getSystemPortName();
                setReaders(portList[i].getSystemPortName());
                comportList.add(portList[i].getSystemPortName());
            }
           }
           catch(Error e)
           {
               
           }
           catch(Exception ex)
           {
               
           }
           setReaders("");
           stopReaderFindProgressBar();
        }
    };

    public void stopReaderFindProgressBar()
    {
        Platform.runLater(new Runnable()
           {
               @Override
               public void run()
               {
                   progressTask.cancel(true);
                   reloadDevicesProgress.progressProperty().unbind();
                   reloadDevicesProgress.setProgress(0);
                   reloadDevicesProgress.visibleProperty().setValue(false);
                   reloadDevicesButton.setDisable(false);
               }
           });
    }
    
    public void setReaders(final String deviceId)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                BorderPane bPane = new BorderPane();
                bPane.setStyle("-fx-background-color: transparent");
                bPane.setId("readerBorderPane");
                bPane.setPrefHeight(40);
                bPane.setMinHeight(40);

                Pane textPane = new Pane();
                if(deviceId.equals(""))
                {
                    customComportField = new TextField();
                    customComportField.setId("deviceText");
                    customComportField.setAlignment(Pos.BASELINE_LEFT);
                    customComportField.setText(deviceId);
                    customComportField.setEditable(true);
                    customComportField.setPromptText("Enter com port");
                    textPane.getChildren().add(customComportField);
                    customComportField.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
                    {

                        @Override
                        public void handle(MouseEvent event)
                        {
                            customComportField.setStyle("-fx-text-inner-color:black");
                        }
                    });
                    customComportField.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() 
                    {

                        @Override
                        public void handle(MouseEvent event)
                        {
                            customComportField.setStyle("-fx-text-inner-color:black");
                            String comport = customComportField.getText();
                            if (comport.length() > 3 && !isConnected)
                            {
                                if (!comport.toUpperCase().startsWith("COM")
                                        && !comport.startsWith("/dev"))
                                {
                                    deviceName = "dummy";
                                    customComportField.setText("");
                                    customComportField.setPromptText("Enter com port");
                                }
                                else if (comportList.contains(comport.toUpperCase()) || comportList.contains(comport))
                                {
                                   showWarningErrorMessage("warning", "Com port already exist in the list");
                                   customComportField.setText("");
                                   customComportField.setPromptText("Enter com port");
                                }
                                else
                                {
                                    deviceName = comport;
                                    HashMap hashMap = (HashMap) comportInfo.get("dummy");
                                    comportInfo.put(deviceName, hashMap);
                                    hideMessagePopup();
                                }
                            }
                        }
                    });
                    customComportField.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() 
                    {
                        @Override
                        public void handle(KeyEvent keyEvent) 
                        {
                            customComportField.setStyle("-fx-text-inner-color:black");
                            String comport = customComportField.getText();
                            if (comport.length() > 3 )
                            {
                                if (!comport.toUpperCase().startsWith("COM")
                                        && !comport.startsWith("/dev")) 
                                {
                                    showWarningErrorMessage("warning", "Please enter valid com port");
                                    customComportField.setStyle("-fx-text-inner-color:red");
                                    keyEvent.consume();
                                }
                            }
                        }
                    });
                } 
                else 
                {
                    Text comportName = new Text();
                    comportName.setId("deviceText");
                    comportName.setTextAlignment(TextAlignment.LEFT);
                    comportName.setText(deviceId);
                    comportName.setLayoutY(20);
                    textPane.getChildren().add(comportName);
                }

                Pane pane = new Pane();
                pane.setMinWidth(100);
                pane.setPrefWidth(100);
                pane.setMaxWidth(100);

                Pane p = new Pane();
                ImageView img = new ImageView(new Image("images/serial-small.png"));
                img.setFitWidth(30);
                img.setFitHeight(30);
                img.setLayoutY(-1);
                img.setOpacity(1);
                ImageView gretarImage = new ImageView(new Image("images/arrow-small.png"));
                gretarImage.setFitHeight(30);
                gretarImage.setFitWidth(20);
                gretarImage.setLayoutX(80);
                gretarImage.setOpacity(0.5);
                gretarImage.setLayoutY(-2);
                p.getChildren().add(img);
                p.getChildren().add(gretarImage);
                p.setOpacity(1);

                ImageView chainImage = new ImageView(new Image("images/link-big-active.png"));
                chainImage.setFitHeight(30);
                chainImage.setFitWidth(30);
                chainImage.setLayoutX(43);
                chainImage.setLayoutY(-2);
                chainImage.setOpacity(0.2);
                pane.getChildren().add(p);
                pane.getChildren().add(chainImage);

                bPane.setLeft(textPane);
                bPane.setRight(pane);
                readerList.getChildren().add(bPane);

                HashMap hashMap = new HashMap<String, Object>();
                hashMap.put("chainImage", chainImage);
                if(deviceId.equals(""))
                {
                    comportInfo.put("dummy", hashMap);
                } 
                else 
                {
                    comportInfo.put(deviceId, hashMap);
                }

                bPane.addEventFilter(
                        MouseEvent.MOUSE_RELEASED,
                        new EventHandler<MouseEvent>()
                        {
                            public void handle(final MouseEvent mouseEvent)
                            {
                                ObservableList<Node> children = readerList.getChildren();
                                for (Node node : children)
                                {
                                    //reverting css for all borderpanes to normal state 
                                    ((BorderPane) node).setStyle("-fx-background-color: transparent");
                                    BorderPane bp = (BorderPane) node;
                                    Pane p = (Pane) bp.getLeft();
                                    if (p.getChildren().get(0).getClass() == Text.class) 
                                    {
                                        Text t = (Text) p.getChildren().get(0);
                                        t.setFill(Color.BLACK);
                                    } 
                                    else
                                    {
                                        TextField t = (TextField) p.getChildren().get(0);
                                        t.setStyle("-fx-text-inner-color:black");
                                    }

                                    Pane borderPaneRightPane = (Pane) bp.getRight();

                                    //getting first childern in right border pane
                                    Pane childern1 = (Pane) borderPaneRightPane.getChildren().get(0);

                                    //getting second childern in right border pane
                                    ImageView childern2ChainImage = (ImageView) borderPaneRightPane.getChildren().get(1);

                                    //getting serial image and arrow image objects from childern1
                                    ImageView childern1SerialImage = (ImageView) childern1.getChildren().get(0);
                                    ImageView childern1ArrowImage = (ImageView) childern1.getChildren().get(1);

                                    childern2ChainImage.setImage(new Image("images/link-big-active.png"));
                                    childern1SerialImage.setImage(new Image("images/serial-small.png"));
                                    childern1ArrowImage.setImage(new Image("images/arrow-small.png"));
                                    childern1ArrowImage.setOpacity(disableOpacity);

                                }
                                //changing selected borderpane css
                                BorderPane bp = (BorderPane) mouseEvent.getSource();
                                bp.setStyle("-fx-background-color: #4F8ABD");
                                Pane p = (Pane) bp.getLeft();
                                if (p.getChildren().get(0).getClass() == Text.class)
                                {
                                    Text t = (Text) p.getChildren().get(0);
                                    t.setFill(Color.WHITE);
                                    deviceName = ((Text) p.getChildren().get(0)).getText();
                                }
                                else
                                {
                                    TextField t = (TextField) p.getChildren().get(0);
                                    t.setStyle("-fx-text-inner-color:white");
                                    HashMap hashMap = (HashMap) comportInfo.get("dummy");
                                    deviceName = ((TextField) p.getChildren().get(0)).getText();

                                    if (false
                                    || deviceName.toUpperCase().startsWith("COM")
                                    || deviceName.startsWith("/dev")) 
                                    {
                                        comportInfo.put(deviceName, hashMap);
                                    } 
                                    else 
                                    {
                                        deviceName = "dummy";
                                    }
                                }

                                Pane borderPaneRightPane = (Pane) bp.getRight();

                                //first childern in right border pane
                                Pane childern1 = (Pane) borderPaneRightPane.getChildren().get(0);

                                //second childern in right border pane
                                ImageView childern2ChainImage = (ImageView) borderPaneRightPane.getChildren().get(1);

                                //getting serial image and arrow image from childern1
                                ImageView childern1SerialImage = (ImageView) childern1.getChildren().get(0);
                                ImageView childern1ArrowImage = (ImageView) childern1.getChildren().get(1);

                                //changing images when selected
                                childern2ChainImage.setImage(new Image("images/link-big-active-select.png"));
                                childern1SerialImage.setImage(new Image("images/serial-small-select.png"));
                                childern1ArrowImage.setImage(new Image("images/arrow-small-select.png"));
                                childern1ArrowImage.setOpacity(buttonEnableOpacity);

                                if (isConnected)
                                {
                                    HashMap hashMap = (HashMap) comportInfo.get(deviceName);
                                    if (hashMap.get("isConnected") != null)
                                    {
                                        connectionButton.setText("Disconnect");
                                        connectionButton.setStyle("-fx-background-color: #D80000");
                                        connectionButton.setDisable(false);
                                        connectionButton.setOpacity(1);
                                        readerProperties.setVisible(true);
                                    }
                                    else
                                    {
                                        readerProperties.setVisible(true);
                                        connectionButton.setText("Connect");
                                        connectionButton.setStyle("-fx-background-color:green");
                                        connectionButton.setDisable(true);
                                        connectionButton.setOpacity(disableOpacity);
                                    }
                                } 
                                else 
                                {
                                    readerProperties.setVisible(true);
                                    connectionButton.setText("Connect");
                                    connectionButton.setStyle("-fx-background-color:green");
                                    connectionButton.setOpacity(buttonEnableOpacity);
                                }
                            }
                        }
                );
            }
        });
    }
    
    @FXML
    private void connect(ActionEvent event) 
    {
        connectionButton.setDisable(true);
        cbTransportLogging.setDisable(true);
        cbTransportLogging.setOpacity(disableOpacity);
        isAutonomousSupported = false;
        try
        {          
            if(deviceName.equals("dummy"))
            {
                showWarningErrorMessage("warning", "Please enter valid comport or select from the list.");
                connectionButton.setDisable(false);
                return;
            }
            
            if (connectionButton.getText().equalsIgnoreCase("Connect"))
            {
                if(cbTransportLogging.isSelected())
                {
                    createTransportLogsIntoFile();
                    isTransportLogsEnabled = true;
                }
                new Thread(connectThread).start();
            }
            else
            {
                new Thread(disConnectThread).start();
            }
        }
        catch (Exception e)
        {
            notifyException(e);
        }
    }
  
    public void showWarningErrorMessage(final String type, final String message)
    {
        Platform.runLater(new Runnable()
        {
            public void run()
            {
                hideMessagePopup();
                popupMsgTitle.setFill(Color.WHITE);
                if (type.equals("warning"))
                {
                   popupMsgPane.setStyle("-fx-background-color:#FF970F");
                   popupMsgTitle.setText("Warning");
                   warningDownArrow.setVisible(true);
                }
                else if (type.equals("error"))
                {
                    popupMsgPane.setStyle("-fx-background-color:#D00006");
                    popupMsgTitle.setText("Error");
                    errorDownArrow.setVisible(true);
                } 
                else
                {
                    popupMsgPane.setStyle("-fx-background-color:#07871C");
                    popupMsgTitle.setText("Success");
                    successDownArrow.setVisible(true);
                }
                popupMsgPane.setVisible(true);
                popupMsgBorderPane.setVisible(true);
                popupMsgContentLabel.setText(message);
            }
        });
    }
 
    void hideMessagePopup()
    {
        warningDownArrow.setVisible(false);
        successDownArrow.setVisible(false);
        errorDownArrow.setVisible(false);
        popupMsgBorderPane.setVisible(false);
        popupMsgPane.setVisible(false);
    }
    
    public void notifyException(final Exception ex)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                customComportField.setEditable(true);
                progressTask.cancel(true);
                connectProgressIndicator.progressProperty().unbind();
                connectProgressIndicator.setProgress(0);
                connectProgressIndicator.visibleProperty().setValue(false);
                reloadDevices.setDisable(false);
                reloadDevicesButton.disableProperty().set(false);
                reloadDevices.setOpacity(1);
                
                connectionButton.setDisable(false);
                probeBaudRate.disableProperty().setValue(false);
                region.disableProperty().setValue(false);
                connectionButton.setText("Connect");
                cbTransportLogging.setDisable(false);
                cbTransportLogging.setOpacity(enableOpacity);
               
                if(ex.getMessage() != null && ex.getMessage().contains("Invalid argument"))
                {
                  showWarningErrorMessage("error", "Port does not exist.");  
                }
                else if(ex.getMessage() != null && ex.getMessage().equalsIgnoreCase(Constants.APPLICATION_IMAGE_FAILED))
                { 
                    isConnected = true;
                    JOptionPane.showMessageDialog(null, ex.getMessage() , "ERROR", JOptionPane.OK_OPTION);
                    mainTabs.getSelectionModel().select(configureTab);
                    setTitledPanesStatus(true, true, true, true, true, false, true);
                    setTitledPanesExpandedStatus(false, false, false, false, false, true, false);
                }
                else if(ex.getMessage() != null)
                {
                  showWarningErrorMessage("error", ex.getMessage());
                }
            }
        });
    }
   
    private Runnable connectThread = new Runnable()
    {
        @Override
        public void run()
        {
            String port = deviceName;
            final HashMap hashMap = (HashMap) comportInfo.get(port);
            final ImageView chainImageView = (ImageView) hashMap.get("chainImage");
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    probeBaudRate.disableProperty().setValue(true);
                    region.disableProperty().setValue(true);
                    reloadDevicesButton.disableProperty().set(true);
                    reloadDevices.setDisable(true);
                    reloadDevices.setOpacity(0.07);
                    connectProgressIndicator.visibleProperty().setValue(true);
                    progressTask = createProgress();
                    connectProgressIndicator.progressProperty().unbind();
                    connectProgressIndicator.progressProperty().bind(progressTask.progressProperty());
                    customComportField.setEditable(false);
                    hideMessagePopup();
                }
            });
            try
            {
                r = Reader.create("tmr:///" + port);
                if(isTransportLogsEnabled)
                {
                    transportListener = new SaveTransportLogs();
                    r.addTransportListener(transportListener);
                }
                exceptionListener = new TagReadExceptionReceiver();
                r.addReadExceptionListener(exceptionListener);
                r.paramSet("/reader/baudRate", Integer.parseInt(probeBaudRate.getSelectionModel().getSelectedItem().toString()));
                r.connect();
                r.removeReadExceptionListener(exceptionListener);
                exceptionListener = null;
                isConnected = true;
                connectedDevice = deviceName ;
            }
            catch (Exception ex)
            {
                notifyException(ex);
                return;
            }

            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        hideMessagePopup();
                        if (isConnected)
                        {

                            setReaderStatus(0);
                            connectionButton.setText("Disconnect");
                            connectionButton.setDisable(false);
                            setTitledPanesStatus(false, false, false, false, false, false, false);
                            setTitledPanesExpandedStatus(true, false, false, false, false, false, false);
                            //setDisableTitledPanes(false);
                            initParams(r, hashMap);
                            connectionButton.setStyle("-fx-background-color: #CE6060");
                            hashMap.put("isConnected", isConnected);
                            chainImageView.setOpacity(1);
                            
                            //Enable after connection
                            applyButton.setDisable(false);
                            applyButton.setOpacity(enableOpacity);
                            revertButton.setDisable(false);
                            revertButton.setOpacity(enableOpacity);
                            loadFirmware.disableProperty().setValue(false);
                            loadFirmware.setOpacity(enableOpacity);

                            loadConfigButton.disableProperty().setValue(false);
                            loadConfigButton.setOpacity(enableOpacity);

                            saveConfigButton.disableProperty().setValue(false);
                            saveConfigButton.setOpacity(enableOpacity);
                            
                            region.setDisable(false);
                            probeBaudRate.setDisable(true);
                            
                            //clear tags
                            clearTags(new ActionEvent());
                            
                            progressTask.cancel(true);
                            connectProgressIndicator.progressProperty().unbind();
                            connectProgressIndicator.setProgress(0);
                            connectProgressIndicator.visibleProperty().setValue(false);
                        }
                    } 
                    catch (Exception ex)
                    {
                        notifyException(ex);
                    }
                }
            });
            return;
        }
    };
    
    private Runnable disConnectThread = new Runnable()
    {
        @Override
        public void run() 
        {
            try
            {
                Platform.runLater(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                        connectTab.setDisable(false);
                        btGetStarted.setDisable(false);
                        connectProgressIndicator.visibleProperty().setValue(true);
                        progressTask = createProgress();
                        connectProgressIndicator.progressProperty().unbind();
                        connectProgressIndicator.progressProperty().bind(progressTask.progressProperty());
                    }
                });
                
                long startTime = System.currentTimeMillis();
                final HashMap hashMap = (HashMap)comportInfo.get(connectedDevice);
                final ImageView chainImageView =(ImageView)hashMap.get("chainImage");
                Platform.runLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        connectProgressIndicator.visibleProperty().setValue(true);
                        progressTask = createProgress();
                        connectProgressIndicator.progressProperty().unbind();
                        connectProgressIndicator.progressProperty().bind(progressTask.progressProperty());
                    }
                });
                if(isAutonomousReadStarted)
                {
                    isReading = false;
                    r.removeReadListener(readListener);
                    r.removeReadExceptionListener(exceptionListener);
                    r.removeStatsListener(statsListener);
                    if(isTransportLogsEnabled)
                    {    
                        r.removeTransportListener(transportListener);
                        isTransportLogsEnabled = false;
                    }
                    isAutonomousReadStarted = false;
                }
               
                try
                {
                    r.destroy();
                    r = null;
                } 
                catch (Exception ex)
                {

                }
                
                Platform.runLater(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                        probeBaudRate.disableProperty().setValue(false);
                        probeBaudRate.setOpacity(enableOpacity);
                        connectionButton.setDisable(false);
                        isConnected = false;
                        reloadDevices.setDisable(false);
                        reloadDevices.setOpacity(1);
                        setReaderStatus(-1);
                        connectionButton.setText("Connect");
                        connectionButton.setStyle("-fx-background-color:green");
                        connectionButton.setOpacity(buttonEnableOpacity);
                        chainImageView.setOpacity(0.2);
                        comportInfo.remove(hashMap);
                        applyButton.setDisable(true);
                        applyButton.setOpacity(disableOpacity);
                        revertButton.setDisable(true);
                        revertButton.setOpacity(disableOpacity);
                        reloadDevicesButton.disableProperty().set(false);
                        progressTask.cancel(true);
                        connectProgressIndicator.progressProperty().unbind();
                        connectProgressIndicator.setProgress(0);
                        connectProgressIndicator.visibleProperty().setValue(false);
                        customComportField.setEditable(true);
                        cbTransportLogging.setDisable(false);
                        cbTransportLogging.setOpacity(enableOpacity);
                        cbTransportLogging.setSelected(false);
                        mainTabs.getSelectionModel().select(connectTab);
                    }
                });
                return ;
            }
            catch(Exception e)
            {
                
            }
        }   
    };
        
    public void initParams(Reader r,HashMap hashMap) throws Exception
    {
        try
        { 
            hideMessagePopup();
            getReaderDiagnostics();
            isSupportsAutonomus(firmwareVerson);
            disableModuleUnsupportedFeatures();

            region.getItems().removeAll(region.getItems());
            currentRegion = (Reader.Region) r.paramGet("/reader/region/id");
            Reader.Region[] supportedRegions = (Reader.Region[]) r.paramGet("/reader/region/supportedRegions");
            //Adding Supported Regions to Hash Map 
            List supportedRegionsList = new ArrayList();
            supportedRegionsList.add(Arrays.asList(supportedRegions));
            hashMap.put("supportedRegions", supportedRegionsList);

            region.getItems().addAll(Arrays.asList(supportedRegions));
            if(currentRegion != null && currentRegion != Reader.Region.UNSPEC)
            {
               isRegionChanged = false;
               region.getSelectionModel().select(currentRegion);
            }
            else
            {
                region.getSelectionModel().clearSelection();
                region.setValue(null);
            }
            
            currentProtocol = (TagProtocol) r.paramGet("/reader/tagop/protocol");
            if(currentProtocol != null && TagProtocol.GEN2 == currentProtocol)
            {
                gen2.setSelected(true);
            }
            else
            {
                gen2.setSelected(false);
            }
            if(!isLoadSaveConfiguration)
            {
                antennaDetection.setSelected(Boolean.parseBoolean(r.paramGet("/reader/antenna/checkPort").toString()));
                configureAntennaBoxes(r);
            }
              regionBasedPowerListener();

            //embeddedMemoryBank.getSelectionModel().select("EPC");

            try
            {    
                Gen2.LinkFrequency lf = (Gen2.LinkFrequency) r.paramGet("/reader/gen2/BLF");
                if (lf == Gen2.LinkFrequency.LINK250KHZ) 
                {
                    link250Khz.setSelected(true);
                } 
                else if (lf == Gen2.LinkFrequency.LINK640KHZ) 
                {
                    link640Khz.setSelected(true);
                }
            }
            catch(Exception e)
            {
                r.paramSet("/reader/gen2/BLF", Gen2.LinkFrequency.LINK250KHZ);
                showWarningErrorMessage("error", "Unknown link frequency found, applying default link frequency to module");
            }

            try
            {
                Gen2.Tari tari = (Gen2.Tari) r.paramGet("/reader/gen2/tari");
                if (tari == Gen2.Tari.TARI_6_25US) 
                {
                    tari6_25us.setSelected(true);
                }
                else if (tari == Gen2.Tari.TARI_12_5US)
                {
                    tari12_5us.setSelected(true);
                }
                else if (tari == Gen2.Tari.TARI_25US) 
                {
                    tari25us.setSelected(true);
                }
            }
            catch(Exception e){
                r.paramSet("/reader/gen2/tari", Gen2.Tari.TARI_6_25US);
                showWarningErrorMessage("error", "Unknown tari found, applying default link frequency to module");
            }

            //storing Gen2 BLF Configuration for change list
            //gen2BlfChangeConfiguration();

            Gen2.Target target = (Gen2.Target) r.paramGet("/reader/gen2/target");

            if (target == Gen2.Target.A) 
            {
                targetA.setSelected(true);
            }
            else if (target == Gen2.Target.AB)
            {
                targetAB.setSelected(true);
            } 
            else if (target == Gen2.Target.B) 
            {
                targetB.setSelected(true);
            }
            else if (target == Gen2.Target.BA)
            {
                targetBA.setSelected(true);
            }

            //storing Gen2 Target Configuration for change list
            //getGen2TargetChangeConfiguration();

            Gen2.TagEncoding tagEncoding = (Gen2.TagEncoding) r.paramGet("/reader/gen2/tagEncoding");
            if (tagEncoding == Gen2.TagEncoding.FM0) 
            {
                fm0.setSelected(true);
            } 
            else if (tagEncoding == Gen2.TagEncoding.M2) 
            {
                m2.setSelected(true);
            }
            else if (tagEncoding == Gen2.TagEncoding.M4) 
            {
                m4.setSelected(true);
            }
            else if (tagEncoding == Gen2.TagEncoding.M8) 
            {
                m8.setSelected(true);
            }

            //storing Gen2 Tag Encoding configuration for change list
            //getGen2TagEncodingChangeConfiguration();

            Gen2.Session session =(Gen2.Session) r.paramGet("/reader/gen2/session");
            if(session == Gen2.Session.S0)
            {
                sessionS0.setSelected(true);
            }
            else if(session == Gen2.Session.S1)
            {
                sessionS1.setSelected(true);
            }
            else if(session == Gen2.Session.S2)
            {
                sessionS2.setSelected(true);
            }
            else if(session == Gen2.Session.S3)
            {
                sessionS3.setSelected(true);
            }
            enableDisableGen2Settings(new ActionEvent());

            Gen2.Q gen2Q = (Gen2.Q)r.paramGet("/reader/gen2/q");
            if(gen2Q instanceof Gen2.DynamicQ)
            {
                dynamicQ.setSelected(true);
            }
            else if(gen2Q instanceof Gen2.StaticQ){
                staticQ.setSelected(true);
                Gen2.StaticQ staticQObj = (Gen2.StaticQ)gen2Q;
                staticQList.getSelectionModel().select(staticQObj.initialQ);
            }
            gen2Q(new ActionEvent());

            //storing session value for change list
            //getGen2SessionChangeConfiguration();
            //showChangeList = true;
       }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getAndSetRegulatorySettingsToUI() throws ReaderException
    {
        setHopTimeToUI();
        setHopTableToUI();
    }

    private void setHopTimeToUI() throws ReaderException
    {
        hopTime.clear();
        int hopTimeValue = (Integer) r.paramGet(TMR_PARAM_REGION_HOPTIME);
        hopTime.setText(""+hopTimeValue);
    }

    private void setHopTableToUI() throws ReaderException
    {
        hopTable.clear();
        int[] hopTableValues = (int[]) r.paramGet(TMR_PARAM_REGION_HOPTABLE);
        hopTable.setText(parseHopTableValues(hopTableValues));
    }

    private String parseHopTableValues(int[] values){
        String string = Arrays.toString(values);
        string = string.substring(1, string.length()-1);
        String stringArray[] = string.split(",");
        int count = 0;
        int totalCoutnt = 0;
        for(int i = 0; i <= stringArray.length-1; i++){
            ++count;
            ++totalCoutnt;
            if(count >= 7 )
            {
             count = 0;
             stringArray[i] = "\n"+stringArray[i];
            }
        }
        hopTable.setPrefRowCount(totalCoutnt/6);
        String output = Arrays.toString(stringArray);
        output = output.substring(1, output.length()-1);
        return output;
    }

    private boolean setAntennaCheckPort(boolean checkPort)
    {
        try
        {
            r.paramSet("/reader/antenna/checkPort", checkPort);
            return true;
        }
        catch(Exception e)
        {
            antennaDetection.setSelected(false);
            showWarningErrorMessage("error", "unsupported parameter");
        }
        return false;
    }

    public void configureAntennaBoxes(Reader r) throws ReaderException
    {
        existingAntennas = new ArrayList<Integer>();
        List<Integer> detectedAntennas = new ArrayList<Integer>();
        List<Integer> validAntenns = new ArrayList<Integer>();
        boolean checkport;
        checkport = Boolean.parseBoolean(r.paramGet("/reader/antenna/checkPort").toString());        
        int[] temp = (int[])r.paramGet("/reader/antenna/portList");
        for (int i=0;i<temp.length;i++) {
            existingAntennas.add(temp[i]);
        }
        int[] temp1 = (int[])r.paramGet("/reader/antenna/connectedPortList");
        for (int i=0;i<temp1.length;i++) 
        {
            detectedAntennas.add(temp1[i]);
        }
        validAntenns = checkport?detectedAntennas:existingAntennas;
        
        CheckBox[] antennaBoxes = {antenna1,antenna2,antenna3,antenna4};
        int antNum =1;
        for (CheckBox cb : antennaBoxes)
        {
            if(existingAntennas.contains(antNum))
            {
                //cb.setDisable(false);
                cb.setVisible(true);
            }
            else
            {
                cb.setVisible(false);
            }
            if(!validAntenns.contains(antNum))
            {
                cb.setDisable(true);
            }
            else
            {
                cb.setDisable(false);
            }
            if(detectedAntennas.contains(antNum))
            {
                cb.setSelected(true);
            }
            else
            {
                cb.setSelected(false);
            }
            antNum++;
        }
    }

    @FXML
    private void findAntennas(ActionEvent event)
    {
        try
        {
            if(currentRegion != Reader.Region.UNSPEC)
            {    
                if(antennaDetection.isSelected())
                {
                   setAntennaCheckPort(true);
                   configureAntennaBoxes(r);
                  // addChangeList("Antenna detection was disabled. \n Now enabled");
                }
                else
                {
                    setAntennaCheckPort(false);
                    configureAntennaBoxes(r);
                  //  addChangeList("Antenna detection was enabled. \n Now disabled");
                }
            }
            else
            {
                antennaDetection.setSelected(false);
                showWarningErrorMessage("warning", "Please select the region");
            }
        }
        catch(ReaderException e)
        {
            e.printStackTrace();
        }
    }

    public void checkBoxListener()
    {

        //By default Auto Read On Gpi Disabled
        autoReadGpi1.setDisable(true);
        autoReadGpi1.setOpacity(disableOpacity);
        autoReadGpi2.setDisable(true);
        autoReadGpi2.setOpacity(disableOpacity);
        autoReadGpi3.setDisable(true);
        autoReadGpi3.setOpacity(disableOpacity);
        autoReadGpi4.setDisable(true);
        autoReadGpi4.setOpacity(disableOpacity);

        // By Default Embedded read data Disabled
        embeddedReadUnique.setDisable(true);
        embeddedReadUnique.setOpacity(disableOpacity);
        embeddedMemoryBank.setDisable(true);
        embeddedMemoryBank.setOpacity(disableOpacity);
        embeddedStart.setDisable(true);
        embeddedStart.setOpacity(disableOpacity);
        embeddedEnd.setDisable(true);
        embeddedEnd.setOpacity(disableOpacity);

        gpiTriggerRead.selectedProperty().addListener(new ChangeListener<Boolean>() 
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
            {
                if (gpiTriggerRead.isSelected()) 
                {
                    changeUIOnGpiTriggerRead(false, enableOpacity);
                } 
                else
                {
                    changeUIOnGpiTriggerRead(true, disableOpacity);
                    autonomousRead.setSelected(false);
                }
            }
        });

        autonomousRead.selectedProperty().addListener(new ChangeListener<Boolean>() 
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
            {
                if(autonomousRead.isSelected())
                {
                    gpiTriggerRead.setSelected(false);
                }
                else
                {
                }
            }
        });

        embeddedReadEnable.selectedProperty().addListener(new ChangeListener<Boolean>() 
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
            {
                if (embeddedReadEnable.isSelected()) 
                {
                    embeddedReadUnique.setDisable(false);
                    embeddedReadUnique.setOpacity(enableOpacity);
                    embeddedMemoryBank.setDisable(false);
                    embeddedMemoryBank.setOpacity(enableOpacity);
                    embeddedMemoryBank.getSelectionModel().select(0);
                    embeddedStart.setDisable(false);
                    embeddedStart.setText("0");
                    embeddedStart.setOpacity(enableOpacity);
                    embeddedEnd.setDisable(false);
                    embeddedEnd.setOpacity(enableOpacity);
                    embeddedEnd.setText("0");
//                    addChangeList("Embedded read was disabled.\n Now enabled");
                    changeListMap.put("embeddedStart", embeddedStart.getText());
                    changeListMap.put("embeddedEnd", embeddedEnd.getText());
                }
                else
                {
//                    addChangeList("Embedded read was enabled.\n Now disabled");
                      disableEmbeddedReadData();
                }
            }
        });
        
       embeddedReadUnique.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if(embeddedReadUnique.isSelected())
                {
//                   addChangeList("Read unique by data was disabled.\n Now enabled");
                }
                else
                {
//                   addChangeList("Read unique by data was enabled.\n Now disabled");
                }
            }
        });
       
        embeddedMemoryBank.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue ov, String t, String t1)
            {
//                addChangeList("Embedded read memory bank was "+t+".\n Now "+ t1);
            }
        });
       
        popupCloseImg.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                warningDownArrow.setVisible(false);
                errorDownArrow.setVisible(false);
                successDownArrow.setVisible(false);
                popupMsgPane.setVisible(false);
                popupMsgBorderPane.setVisible(false);
            }
        });

        region.valueProperty().addListener(new ChangeListener<Reader.Region>()
        {
            @Override
            public void changed(ObservableValue<? extends Reader.Region> observable, Reader.Region oldValue, Reader.Region newValue) 
            {
                if(newValue != null && newValue != Reader.Region.UNSPEC)
                {
                    try
                    {
                        if(oldValue != newValue && isRegionChanged)
                        {
                            r.paramSet("/reader/region/id", newValue);
                        }
                        else if(!isRegionChanged)
                        {
                           isRegionChanged = true;
                        }
                        currentRegion = newValue;
                        regionBasedPowerListener();
                        getAndSetRegulatorySettingsToUI();
                    }
                    catch(Exception e)
                    {
                        showWarningErrorMessage("error","invalid or unsupported parameter");
                    }
                }
                else
                {
                    region.getSelectionModel().clearSelection();
                }
            }
      });
      
       gen2.selectedProperty().addListener(new ChangeListener<Boolean>() 
       {
             @Override
             public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
             {
                if(newValue)
                {
                    try
                    {    
                        r.paramSet("/reader/tagop/protocol", TagProtocol.GEN2);
                    }
                    catch(Exception e)
                    {
                        
                    }    
                }
             }
      });

    }

    public void powerSlider()
    {
        readPowerSlider.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event) 
            {
                powerSliderListener(readPowerSlider, writePowerSlider, rfRead, rfWrite);
                //readPowerChanged();
            }
        });
        
        readPowerSlider.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event) 
            {
                powerSliderListener(readPowerSlider, writePowerSlider, rfRead, rfWrite);
                readPowerChanged();
                writePowerChanged();
            }
        });
        writePowerSlider.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event) 
            {
                powerSliderListener(writePowerSlider, readPowerSlider, rfWrite, rfRead);
                writePowerChanged();
                readPowerChanged();
            }
        });
        
        writePowerSlider.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>()
        {

            @Override
            public void handle(MouseEvent event) 
            {
                powerSliderListener(writePowerSlider, readPowerSlider, rfWrite, rfRead);
            }
        });
        
        powerEquator.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
            {
                if (powerEquator.isSelected())
                {
                    if(rfRead.getText().equals(""))
                    {
                       rfRead.setText(""+minReaderPower);
                    }
                    if(rfWrite.getText().equals(""))
                    {
                       rfWrite.setText(""+minReaderPower);
                    }
                    double d = Math.max(Double.parseDouble(rfRead.getText()), Double.parseDouble(rfWrite.getText()));
                    String text = "" + new DecimalFormat("##.##").format(d);
                    readPowerSlider.setValue(d);
                    writePowerSlider.setValue(d);
                    rfRead.setText(text);
                    rfRead.positionCaret(text.length());
                    rfWrite.setText(text);
                    rfWrite.positionCaret(text.length());
                    readPowerChanged();
                    writePowerChanged();
                } 

            }
        
        });
        
        rfRead.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue) 
                {
                    powerFocusedlistener(rfRead, rfWrite, readPowerSlider);
                    readPowerChanged();
                }
            }
        });
        
        rfWrite.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
            {
                if(!newValue) 
                {
                    powerFocusedlistener(rfWrite, rfRead, readPowerSlider);
                    writePowerChanged();
                }
            }
        });
        
        rfRead.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent event) 
            {
                powerEventListener(readPowerSlider, writePowerSlider, rfRead, rfWrite);
                readPowerChanged();
            }
        });
        
        rfWrite.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent event) 
            {
                powerEventListener(writePowerSlider, readPowerSlider, rfWrite,rfRead);
               writePowerChanged();
            }
        });
        
        rfRead.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>()
        {            @Override
            public void handle(MouseEvent event) 
            {
                String text = rfRead.getText();
                if("".equals(text) || text.startsWith("-."))
                {
                    powerValueChanged(readPowerSlider, writePowerSlider, rfRead, rfWrite);
                }
            }
        });
      
        rfWrite.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>()
        {            @Override
            public void handle(MouseEvent event) 
            {
                String text = rfWrite.getText();
                if("".equals(text) || text.startsWith("-."))
                {
                   powerValueChanged(writePowerSlider, readPowerSlider, rfWrite, rfRead);
                }
            }
        });
    }
    
    public void powerValueChanged(Slider slider1, Slider slider2, TextField field1, TextField field2)
    {
        double value = slider1.getValue();
        
        if (powerEquator.isSelected())
        {
            if (value <= writePowerSlider.getValue())
            {
                field1.setText(""+slider2.getValue());
                field2.setText(""+slider2.getValue());
            }
            else
            {
                field2.setText(""+slider1.getValue());
            }
        } 
        else
        {
            field1.setText(""+slider1.getValue());
        }
    }

    public void powerEventListener(Slider slider1, Slider slider2, TextField field1, TextField field2)
    {
        String text = field1.getText();
        //String text1=field2.getText();
        String tempText = "";
        int length = text.length();
        if(minReaderPower < 0 && (text.startsWith("-") || text.startsWith(".")))
        {
          if(text.length() > 1)
          {
             tempText = text.substring(1);
             if(tempText.contains("-"))
             {
               tempText = tempText.replaceAll("[^0-9.]", "");
               field1.setText("-"+tempText);
               field1.selectPositionCaret(("-"+tempText).length());
             }             
             text = tempText;
          }
          else
          {
              return;
          }
        }
        
        if (length > 0)
        {
            if (!text.matches("^[0-9]+([\\.][0-9]+)?$"))
            {
                int index = text.indexOf(".");
                text = text.substring(0, index + 1) + text.substring(index + 1, text.length()).replace(".", "");
                text = text.replaceAll("[^0-9.]", "");
                if (tempText.length() >= 1)
                {
                    field1.setText("-"+text);
                    field1.positionCaret(text.length()+1);
                }
                else
                {
                   field1.setText(text);
                   field1.positionCaret(text.length());
                }
            }
            
            text = field1.getText();
           
            double d = 0,d2=0;
            if (text != null && (!"".equalsIgnoreCase(text) && !text.startsWith("-.")))
            {
                d = Double.parseDouble(text);                
            }
            if (d <= slider1.getMax())
            {
                if (powerEquator.isSelected())
                {
                    slider1.setValue(d);                    
                    field2.setText(text);
                    field2.positionCaret(text.length());
                } 
                else
                {
                    slider1.setValue(d);                    
                }
            }
            else
            {
                if (powerEquator.isSelected())
                {
                    slider1.setValue(slider1.getMax());
                    slider2.setValue(slider1.getMax());
                    text = "" + slider1.getMax();
                    field1.setText(text);
                    field1.positionCaret(text.length());
                    field2.setText(text);
                    field2.positionCaret(text.length());
                } 
                else
                {
                    slider1.setValue(slider1.getMax());
                    text = "" + slider1.getMax();
                    field1.setText(text);
                    field1.positionCaret(text.length());
                }
            }
        } 
        else
        {
            if (powerEquator.isSelected())
            {
                field2.setText("");
                slider1.setValue(slider1.getMin());
                slider2.setValue(slider1.getMin());
            }
        }
    }
    
    public void readPowerChanged()
    {
        String prevPower = changeListMap.get("readPower");
        String currentPower = rfRead.getText();
        if (!prevPower.equals(currentPower))
        {
            changeListMap.put("readPower", currentPower);
//            addChangeList("Read power changed from " + prevPower + " dBm to " + currentPower + " dBm.");
        }
        checkReadWritePowerOnUSBProModule();
    }
    
    public void writePowerChanged()
    {
        String prevPower = changeListMap.get("writePower");
        String currentPower = rfWrite.getText();
        if (!prevPower.equals(currentPower))
        {
            changeListMap.put("writePower", currentPower);
//            addChangeList("Write power changed from " + prevPower + " dBm to " + currentPower + " dBm.");
        }
        checkReadWritePowerOnUSBProModule();
    }
    
    public void powerFocusedlistener(TextField field1, TextField field2, Slider slider)
    {
        String text = field1.getText();
        if (text.equals(""))
        {
            text = "0";
        }
        else if((text.startsWith("-") || text.startsWith(".")) && text.length() == 1)
        {
            text = "0";
            field1.setText(text);
        }

        double d = Double.parseDouble(text);
        if (d < slider.getMin())
        {
            text = "" + slider.getMin();
            field1.setText(text);
            field1.positionCaret(text.length());

            if (powerEquator.isSelected())
            {
                field2.setText(text);
                field2.positionCaret(text.length());
            }
        }
    }
    
    public void powerSliderListener(Slider slider1, Slider slider2, TextField text1, TextField text2)
    {
        double d = slider1.getValue();
        String text = "" + new DecimalFormat("##.##").format(d);

        if (powerEquator.isSelected())
        {
            slider2.setValue(d);
            text1.setText(text);
            text1.positionCaret(text.length());
            text2.setText(text);
            text2.positionCaret(text.length());
        } 
        else
        {
            text1.setText(text);
            text1.positionCaret(text.length());
        }  
    }

    public void validateTextFields() 
    {
        // Listener for DutyCycle On TextField
        dutyCycleOn.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                try
                {
                    if ((isDecimal(newValue)))
                    {
                        if(newValue.length() > 5 || Integer.parseInt(newValue) > 65535)
                        {
                            showWarningErrorMessage("error", "Please input dutycycle on timeout less than 65535");
                            dutyCycleOn.setText("1000");
                        }
                        else
                        {
                        dutyCycleOn.setText(newValue);
                        }
                    }
                    else
                    {
                        dutyCycleOn.setText("");
                    }
                }
                catch(Exception e)
                {
                    dutyCycleOn.setText("");
                }
            }
        });

        // Listener for DutyCycle Off  TextField
        dutyCycleOff.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) 
            {
                try
                {
                    if ((isDecimal(newValue)))
                    {
                        if(newValue.length() > 5 || Integer.parseInt(newValue) > 65535)
                        {
                            showWarningErrorMessage("error", "Please input dutycycle off timeout less than 65535");
                            dutyCycleOff.setText("0");
                        }
                        else
                        {
                        dutyCycleOff.setText(newValue);
                        }
                    }
                    else
                    {
                        dutyCycleOff.setText("");
                    }
                }
                catch(Exception e)
                {
                    dutyCycleOff.setText("");
                }
            }
        });
        
        embeddedStart.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent keyEvent) 
            {
                String ch = keyEvent.getCharacter();
                if (!"0123456789xX".contains(ch) || embeddedStart.getText().length() > 9) 
                {
                    keyEvent.consume();
                }
            }
        });

        embeddedEnd.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() 
        {
            @Override
            public void handle(KeyEvent keyEvent)
            {
                String ch = keyEvent.getCharacter();
                if (!"0123456789xX".contains(ch) || embeddedEnd.getText().length() > 3)
                {
                      keyEvent.consume();
                   }
                }
        });
        
        embeddedStart.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() 
        { 
            public void handle(MouseEvent event)
            {
                String addr = embeddedStart.getText();
                if(addr.length() == 0)
                {
                   embeddedStart.setText("0");
                }
                else if(addr.toLowerCase().startsWith("0x"))
                {
                    if(addr.substring(2).contains("x"))
                    {
                        showWarningErrorMessage("warning", "Invalid value for start address");
                        embeddedStart.setText("0");
                    }
                }
            }
        });

        embeddedEnd.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() 
        { 
            public void handle(MouseEvent event)
            {
                String addr = embeddedEnd.getText();
                if(addr.length() == 0)
                {
                   embeddedEnd.setText("0");
                }
                else if(addr.toLowerCase().startsWith("0x"))
                {
                    if(addr.substring(2).contains("x"))
                    {
                        showWarningErrorMessage("warning", "Invalid value for length");
                        embeddedEnd.setText("0");
                    }
                }
            }
        });

        hopTime.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent keyEvent) 
            {
                String ch = keyEvent.getCharacter();
                if (!"0123456789".contains(ch)) 
                {
                    keyEvent.consume();
                }
            }
        });

        hopTable.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent keyEvent) 
            {
                String ch = keyEvent.getCharacter();
                if (!"0123456789".contains(ch)) 
                {
                    keyEvent.consume();
                }
            }
        });
    }

    public boolean isNumber(String value)
    {
        String reg = "[0-9]+";
        return value.matches(reg);
    }

    public boolean isDecimal(String value) 
    {
        String reg = "[0-9]+(\\.[0-9][0-9]?)?";
        return value.matches(reg);
    }

    public boolean isInteger(String value) 
    {
        try
        {
            Integer.parseInt(value);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public boolean isIntegerArray(String[] values) 
    {
        for(String value : values){
            if(isInteger(value.trim()))
            {
             //Continue
            }else{
                return false;
            }
        }
        return true;
    }

    //Below methods for showing change list for all Configurations

    @FXML
    private void protocolChangeConfiguration(ActionEvent event)
    {
        
        String previousInfo = "Previous selected protocols :"+ changeListMap.get("protocol");
//        getSelectedProtocols();
//        addChangeList(previousInfo +"\nNow selected : "+changeListMap.get("protocol"));
    }

    public void getSelectedProtocols()
    {
        String  protocolsStatusInfo = "";
        if(gen2.isSelected())
        {
            protocolsStatusInfo += " GEN2,";
        }
        if(iso18000.isSelected())
        {
            protocolsStatusInfo += " ISO18000-6B,";
        }
        if(ipx64.isSelected())
        {
            protocolsStatusInfo += " IPX64,";
        }
        if(ipx256.isSelected())
        {
            protocolsStatusInfo += " IPX256,";
        }
       
        if(protocolsStatusInfo.length() == 0)
        {
           protocolsStatusInfo = "NONE,";
        }
        protocolsStatusInfo = protocolsStatusInfo.substring(0, protocolsStatusInfo.length() - 1);
        changeListMap.put("protocol",protocolsStatusInfo);
    }

    @FXML
    private void antennaChangeConfiguration(ActionEvent event)
    {
        getAntennaChangeConfiguration();
    }
    
    public void getAntennaChangeConfiguration()
    {
        String prevAntenna = changeListMap.get("antenna");
        String info = "Previous selected antennas :"+ prevAntenna;
//        getSelectedAntennas();
//        if (!changeListMap.get("antenna").equals(prevAntenna))
//        {
//            addChangeList(info + "\nNow selected : " + changeListMap.get("antenna"));
//        }
    }
    
    public void  getSelectedAntennas()
    {
      String  antennas = "";
      if(antenna1.isSelected())
      {
         antennas += " 1,"; 
      }
      if(antenna2.isSelected())
      {
          antennas += " 2,"; 
      }
      if(antenna3.isSelected())
      {
       antennas += " 3,";    
      }
      if(antenna4.isSelected())
      {
         antennas += " 4,";  
      }
      if(antennas.length() == 0)
      {
        antennas = "NONE,";
      }
      antennas = antennas.substring(0, antennas.length()-1);
      changeListMap.put("antenna", antennas);
    }
    
    @FXML
    private void autonomousReadChangeConfiguration(ActionEvent event)
    {
//        if(autonomousRead.isSelected())
//        {
//            addChangeList("Autonomous read was disabled"+"\nNow Enabled");
//        }
//        else
//        {
//            addChangeList("Autonomous read was Enabled"+"\nNow disabled");
//        }
        
    }
    
    @FXML
    private void getEmbeddedStartChangeConfiguration(KeyEvent event)
    {
//        addChangeList("Embedded Start previous value :"+changeListMap.get("embeddedStart")+" Now: "+embeddedStart.getText());
        changeListMap.put("embeddedStart", embeddedStart.getText());
    }
    
    @FXML
    private void getEmbeddedEndChangeConfiguration(KeyEvent event)
    {
//        addChangeList("Embedded End previous value: "+changeListMap.get("embeddedEnd")+" Now: "+embeddedEnd.getText());
        changeListMap.put("embeddedEnd", embeddedEnd.getText());
    }
    
    @FXML
    private void gpiTriggerReadChangeConfiguration(ActionEvent event)
    {
        if (gpiTriggerRead.isSelected())
        {
//            addChangeList("GPI Trigger read was disabled \nNow: Enabled " );
        }
        else
        {
//            addChangeList("GPI Trigger read was enabled \nNow: Disabled" );
        }
    }
    
    public void gpiPinChangeConfiguration()
    {
        changeListMap.put("gpiPin", autoReadGpiGroup.getSelectedToggle().getUserData().toString());
    }
  
    public void gen2BlfChangeConfiguration()
    {
//       String prevLinkFreq =   changeListMap.get("linkFreq");
//       String currentLinkFreq = linkFreqGroup.getSelectedToggle().getUserData().toString();
//       changeListMap.put("linkFreq", currentLinkFreq);
//       if(!(prevLinkFreq != null && currentLinkFreq.equals(prevLinkFreq)))
//       {
//           addChangeList("Link frequency was "+prevLinkFreq +"\n Now "+ currentLinkFreq);
//       }
    }
    
    public void gen2TagEncodingChangeConfiguration()
    {
//        String previousInfo = changeListMap.get("tagEncoding");
//        getGen2TagEncodingChangeConfiguration();
//        if(!changeListMap.get("tagEncoding").equals(previousInfo))
//        {
//          addChangeList("Tag encoding was "+previousInfo+"\n Now "+changeListMap.get("tagEncoding"));
//        }
    }

    public void getGen2TagEncodingChangeConfiguration()
    {
        changeListMap.put("tagEncoding", tagEncodeGroup.getSelectedToggle().getUserData().toString());
    }

    public void getGen2SessionChangeConfiguration()
    {
        changeListMap.put("session", sessionGroup.getSelectedToggle().getUserData().toString());
    }

    @FXML
    private void gen2TargetChangeConfiguration()
    {
        String previousInfo = changeListMap.get("target");
//        getGen2TargetChangeConfiguration();
//        addChangeList("Gen2 Target was "+previousInfo+"\n Now "+changeListMap.get("target"));
    }

    public void getGen2TargetChangeConfiguration()
    { 
        changeListMap.put("target", targetGroup.getSelectedToggle().getUserData().toString());
    }

    @FXML
    private void gen2Q(ActionEvent event){
        if(staticQ.isSelected()){
            staticQList.setDisable(false);
            staticQList.setOpacity(enableOpacity);
        }else{
            staticQList.setDisable(true);
            staticQList.setOpacity(disableOpacity);
        }
    }

    @FXML
    private void dispalyChangeConfiguration(ActionEvent event)
    {
        String previousInfo = changeListMap.get("displayOption");
//        getDisplayOptions();
//        addChangeList("Display options was  "+previousInfo+"\nNow "+changeListMap.get("displayOption"));
    }
    
    public void getDisplayOptions()
    {
        String displayOptionInfo = "";
        if(metaDataAntenna.isSelected())
        {
            displayOptionInfo += " Antenna";
        }
        if(metaDataProtocol.isSelected())
        {
            displayOptionInfo += " Protocol";
        }
        if(metaDataFrequency.isSelected())
        {
            displayOptionInfo += " Frequency";
        }
        if(metaDataPhase.isSelected())
        {
            displayOptionInfo += " Phase";
        }
        changeListMap.put("displayOption", displayOptionInfo);
    }
    
//    public void addChangeList(String information)
//    {
//        if(showChangeList)
//        {
//            final Label label = new Label();
//            label.setText(information);
//            label.setWrapText(true);
//            ImageView img = new ImageView(new Image("/images/cancel.png"));
//            Button button = new Button("", img);
//            label.setGraphic(button);
//            changeListContainer.getChildren().add(label);
//            button.setOnAction(new EventHandler<ActionEvent>()
//            {
//                @Override
//                public void handle(ActionEvent event)
//                {
//                    changeListContainer.getChildren().remove(label);
//                }
//            });
//        }
//    }
    
    @FXML
    private void clearChageList(ActionEvent event)
    {
        changeListContainer.getChildren().clear();
    }

    @FXML
    private void enableDisableGen2Settings(ActionEvent event) 
    {
        if (link640Khz.isSelected()) 
        {
            fm0.setSelected(true);
            tari6_25us.setSelected(true);

            m2.setSelected(false);
            m4.setSelected(false);
            m8.setSelected(false);
            tari25us.setSelected(false);
            tari12_5us.setSelected(false);

            m2.setDisable(true);
            m4.setDisable(true);
            m8.setDisable(true);
            tari25us.setDisable(true);
            tari12_5us.setDisable(true);

            m2.setOpacity(disableOpacity);
            m4.setOpacity(disableOpacity);
            m8.setOpacity(disableOpacity);
            tari25us.setOpacity(disableOpacity);
            tari12_5us.setOpacity(disableOpacity);

        }
        else if (link250Khz.isSelected()) 
        {
            if (readerModel.contains("M5e") || readerModel.equalsIgnoreCase("M6e Nano"))
            {
                fm0.setDisable(true);
                tari6_25us.setDisable(true);
                tari12_5us.setDisable(true);
                fm0.setOpacity(disableOpacity);
                tari6_25us.setOpacity(disableOpacity);
                tari12_5us.setOpacity(disableOpacity);
            }
            else
            {
                fm0.setDisable(false);
                tari6_25us.setDisable(false);
                tari12_5us.setDisable(false);
                fm0.setOpacity(enableOpacity);
                tari6_25us.setOpacity(enableOpacity);
                tari12_5us.setOpacity(enableOpacity);
            }

            m2.setDisable(false);
            m4.setDisable(false);
            m8.setDisable(false);
            tari25us.setDisable(false);

            m2.setOpacity(enableOpacity);
            m4.setOpacity(enableOpacity);
            m8.setOpacity(enableOpacity);
            tari25us.setOpacity(enableOpacity);
        }
    }

    @FXML
    private void applyConfigurations(ActionEvent event) 
    {
        hideMessagePopup();
        String message = "Applying selected configurations on module.";
      
        if(region.getSelectionModel().getSelectedItem() == null || "".equals(region.getSelectionModel().getSelectedItem()))
        {
            showWarningErrorMessage("warning", "Please select valid region");
            return;
        }
        
        if(!gen2.isSelected())
        {
           showWarningErrorMessage("warning", "Please select Gen2 protocol");
           return; 
        }
        if (autonomousRead.isSelected() || gpiTriggerRead.isSelected())
        {
            message = "Applying configurations along with autonomous read will navigate to tag results.";

        }
        int option = JOptionPane.showConfirmDialog(null, message, "Confirmation", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION)
        {
                applyConfigurationsToModule();
        }
    }
    
    void applyConfigurationsToModule()
    {
        if (isConnected) 
        {
            try
            {
                int len = 0;
                int startAddr = 0;
                GpiPinTrigger gpiPinTrigger = new GpiPinTrigger();
                List<Integer> antennaList = new ArrayList<Integer>();
                List<TagProtocol> protocolList = new ArrayList<TagProtocol>();
                TagProtocol protocol = null;
                Gen2.LinkFrequency linkFreq = null;
                Gen2.Tari tari = null;
                Gen2.Target target = null;
                Gen2.TagEncoding tagEncoding = null;
                Gen2.Session session = null;
                Gen2.Q q = null;
                TagOp Op = null;
                int asyncOnTime, asyncOffTime;

                if(region.getSelectionModel().getSelectedItem() != null)
                {
                    r.paramSet("/reader/region/id", Reader.Region.valueOf(region.getSelectionModel().getSelectedItem().toString()));
                }
                r.paramSet("/reader/baudRate", Integer.parseInt(probeBaudRate.getSelectionModel().getSelectedItem().toString()));

                if (antenna1.isSelected()) 
                {
                    antennaList.add(1);
                }
                if (antenna2.isSelected()) 
                {
                    antennaList.add(2);
                }
                if (antenna3.isSelected()) 
                {
                    antennaList.add(3);
                }
                if (antenna4.isSelected()) 
                {
                    antennaList.add(4);
                }

                if((autonomousRead.isSelected() || gpiTriggerRead.isSelected()) && antennaList.isEmpty())
                {
                   showWarningErrorMessage("warning", "Select atleast one antenna");
                   Platform.runLater(new Runnable() 
                   {
                       @Override
                       public void run() 
                       {
                           readWriteTitledPane.setExpanded(true);
                       }
                   });
                   return;
                }

                if (gen2.isSelected()) 
                {
                    protocolList.add(TagProtocol.GEN2);
                    r.paramSet("/reader/tagop/protocol", TagProtocol.GEN2);
                }
                if (iso18000.isSelected()) 
                {
                    protocolList.add(TagProtocol.ISO180006B);
                }
                if (ipx64.isSelected()) 
                {                  
                    protocolList.add(TagProtocol.IPX64);
                }
                if (ipx256.isSelected()) 
                {
                    protocolList.add(TagProtocol.IPX256);
                }
              
                if(embeddedReadEnable.isSelected())
                {
                    
                    if (embeddedEnd.getText().toLowerCase().startsWith("0x"))
                    {
                       len = Integer.parseInt(embeddedEnd.getText().substring(2),16);
                    }
                    else
                    {
                       len = Integer.parseInt(embeddedEnd.getText());
                    }
                   
                    if (embeddedStart.getText().toLowerCase().startsWith("0x"))
                    {
                        startAddr = Integer.parseInt(embeddedStart.getText().substring(2),16);
                    }
                    else
                    {
                       startAddr = Integer.parseInt(embeddedStart.getText());   
                    }
                    
                    if(len > 100)
                    {
                        showWarningErrorMessage("error", "Embedded Read Data: Number of words can't be more than 0x64 words,it can be"
                          +" read by incrementing start address and length of target read data");
                        return;
                    }
                    
                    if(startAddr > 0xFFFF)
                    {
                       showWarningErrorMessage("error", "Embedded Read Data: Starting Word Address can't be more than 0xFFFF");
                        return;
                    }
                }
                
                if(false
                        || iso18000.isSelected()
                        || ipx64.isSelected()
                        || ipx256.isSelected())
                {
                    if (gpiTriggerRead.isSelected() || autonomousRead.isSelected())
                    {
                        showWarningErrorMessage("warning", "Autonomous read and Gpi trigger are not supported other than GEN2 protocol."
                                + " \n Application will read only Gen2 tags.");
                        iso18000.setSelected(false);
                        ipx64.setSelected(false);
                        ipx256.setSelected(false);
                        if(!gen2.isSelected())
                        {
                            gen2.setSelected(true);
                            protocolList.add(TagProtocol.GEN2);
                        }
                    }
                }
                if(protocolList.isEmpty())
                {
                   showWarningErrorMessage("warning","Select Gen2 protocol");
                   return;
                }
                
                if(dutyCycleOn.getText() == null || dutyCycleOn.getText().equals(""))
                {
                    showWarningErrorMessage("error", "DutyCycle On(ms) can't be empty");
                    dutyCycleOn.setText("1000");
                    return;
                }
                if(dutyCycleOff.getText() == null || dutyCycleOff.getText().equals("")) 
                {
                    showWarningErrorMessage("error", "DutyCycle Off(ms) can't be empty");
                    dutyCycleOff.setText("0");
                    return;
                }
                
                asyncOnTime = Integer.parseInt(dutyCycleOn.getText());    
                asyncOffTime = Integer.parseInt(dutyCycleOff.getText());
                
                r.paramSet("/reader/read/asyncOnTime",asyncOnTime);    
                r.paramSet("/reader/read/asyncOffTime",asyncOffTime);

                linkFreq = Gen2.LinkFrequency.valueOf(linkFreqGroup.getSelectedToggle().getUserData().toString());
                tari = Gen2.Tari.valueOf(tariGroup.getSelectedToggle().getUserData().toString());
                tagEncoding = Gen2.TagEncoding.valueOf(tagEncodeGroup.getSelectedToggle().getUserData().toString());
                target = Gen2.Target.valueOf(targetGroup.getSelectedToggle().getUserData().toString());
                session = Gen2.Session.valueOf(sessionGroup.getSelectedToggle().getUserData().toString());

                int qSelectedOption =  Integer.parseInt(qGroup.getSelectedToggle().getUserData().toString());
                if(qSelectedOption == 0){
                    q = new Gen2.DynamicQ();
                }
                else if(qSelectedOption == 1){
                    int staticQvalue = Integer.parseInt(staticQList.getSelectionModel().getSelectedItem().toString());
                    q = new Gen2.StaticQ(staticQvalue);
                }

                if (gpiTriggerRead.isSelected()) 
                {
                    int[] gpiPin = new int[1];
                    gpiPin[0] = Integer.parseInt(autoReadGpiGroup.getSelectedToggle().getUserData().toString());
                    gpiPinTrigger.enable = true;
                    try
                    {
                       r.paramSet("/reader/read/trigger/gpi", gpiPin);
                    }
                    catch (Exception ex)
                    {
                        if(false
                            || ex.getMessage().contains("The reader received a valid command with an unsupported or invalid parameter")
                            || ex.getMessage().contains("The data length in the message is less than or more"))
                        {
                           showWarningErrorMessage("warning","Gpi trigger read is not supported on this firmware");
                           autonomousRead.selectedProperty().setValue(false);
                           return ;
                        }
                        else
                        {
                            showWarningErrorMessage("error", ex.getMessage());
                            return ;
                        }
                    }
                }

                int readPower = (int) (Double.parseDouble(rfRead.getText()) * 100);
                int writePower = (int) (Double.parseDouble(rfWrite.getText()) * 100);

                r.paramSet("/reader/gen2/BLF", linkFreq);
                r.paramSet("/reader/gen2/tari", tari);
                r.paramSet("/reader/gen2/target", target);
                r.paramSet("/reader/gen2/tagEncoding", tagEncoding);
                r.paramSet("/reader/gen2/session", session);
                r.paramSet("/reader/gen2/q", q);
                r.paramSet("/reader/radio/readPower", readPower);
                r.paramSet("/reader/radio/writePower", writePower);

                SimpleReadPlan srp = null;
                ReadPlan[] rp = new ReadPlan[protocolList.size()];

                if (embeddedReadEnable.isSelected() && (autonomousRead.isSelected() || gpiTriggerRead.isSelected())) 
                {
                    if(antennaList.size() > 0)
                    {
                       r.paramSet("/reader/tagop/antenna", antennaList.get(0)); 
                    }
                    
                    String memBank = embeddedMemoryBank.getSelectionModel().getSelectedItem().toString();
                    if (len < 0) {
                        len = 0;
                    }
                    Op = new Gen2.ReadData(Gen2.Bank.valueOf(memBank), startAddr, (byte) len);                    
                }
                int i = 0;
                for (TagProtocol proto : protocolList)
                {
                    srp = new SimpleReadPlan(ReaderUtil.buildIntArray(antennaList), proto, null, Op, 100, false);
                    rp[i] = srp;
                    i++;
                }

                if (autonomousRead.isSelected())
                {
                    srp.enableAutonomousRead = true;
                }
                if(gpiTriggerRead.isSelected())
                {
                    srp.enableAutonomousRead = true;
                    srp.triggerRead = gpiPinTrigger;
                }

                try
                {
                    SerialReader.ReaderStatsFlag[] READER_STATISTIC_FLAGS = {SerialReader.ReaderStatsFlag.TEMPERATURE};
                    r.paramSet("/reader/stats/enable", READER_STATISTIC_FLAGS);
                }
                catch (Exception ex)
                {
                    /*Ignore the exception if reader stats does not support on current reader.
                     *Still need to do autonomous read. 
                     */
                }
                try
                {
                    setHopTime();
                }
                catch(Exception ex)
                {
                    showWarningErrorMessage("error", ex.getMessage());
                    return;
                }

                try
                {
                  setHopTable();
                }
                catch(Exception ex)
                {
                    showWarningErrorMessage("error", ex.getMessage());
                    return;
                }

                if (autonomousRead.isSelected() || gpiTriggerRead.isSelected())
                {
                    if (protocolList.size() > 1)
                    {
                        MultiReadPlan mrp = new MultiReadPlan(rp);
                        mrp.enableAutonomousRead = true;
                        r.paramSet("/reader/read/plan", mrp);
                    } 
                    else
                    {
                        r.paramSet("/reader/read/plan", srp);
                    }
                    
                    try
                    {
                        r.paramSet("/reader/userConfig", new SerialReader.UserConfigOp(SerialReader.SetUserProfileOption.SAVEWITHREADPLAN));
                        r.paramSet("/reader/userConfig", new SerialReader.UserConfigOp(SerialReader.SetUserProfileOption.RESTORE));
                      
                        setTitledPanesStatus(true, true, true, false, true, true, false);
                        setTitledPanesExpandedStatus(false, false, false, true, false, false, true);

                        applyButton.setDisable(true);
                        applyButton.setOpacity(disableOpacity);
                        revertButton.setDisable(true);
                        revertButton.setOpacity(disableOpacity);
                        isAutonomousReadStarted = true;

                        read();
                        isReading = true;
                        setReaderStatus(1);
                        r.receiveAutonomousReading();
                        mainTabs.getSelectionModel().select(readTab);
                        tableView.getItems().clear();
                        uniqueTagCount.setText("");
                        totalTagCount.setText("");
                        new Thread(dataPostThread).start();
                    }
                    catch (Exception ex)
                    {
                        if(false 
                            || ex.getMessage().contains("/reader/userConfig")
                            || ex.getMessage().contains("The reader received a valid command with an unsupported or invalid parameter")
                            || ex.getMessage().contains("The data length in the message is less than or more"))
                        {
                           showWarningErrorMessage("warning","Autonomous read is not supported on this fimware");
                           autonomousRead.selectedProperty().setValue(false);
                        }
                        else
                        {
                            showWarningErrorMessage("error", ex.getMessage());
                        }
                    }
                }
                else
                {
                    if(!antennaList.isEmpty())
                    {
                        srp.enableAutonomousRead = false;
                        srp.antennas = ReaderUtil.buildIntArray(antennaList);
                        r.paramSet("/reader/read/plan", srp);
                        r.paramSet("/reader/userConfig", new SerialReader.UserConfigOp(SerialReader.SetUserProfileOption.SAVEWITHREADPLAN));
                        showWarningErrorMessage("success", "Configurations applied successfully");
                    }
                    else
                    {
                        showWarningErrorMessage("error", "Please select atleast one antenna to save the configurations with autonomous read enabled or disabled");
                    }
                }
            }
            catch (Exception re)
            {
                re.printStackTrace();
                showWarningErrorMessage("error", re.getMessage());
            }
        }
    }
 
    private void setHopTime() throws ReaderException
    {
        String hotTimeTxt = hopTime.getText();
        if(!isInteger(hotTimeTxt)){
            
            throw new ReaderException("Hoptime value was either too large or too small for int");
        }
        int hopTime = Integer.parseInt(hotTimeTxt);
        r.paramSet("/reader/region/hopTime", hopTime);
    }

    private void setHopTable() throws ReaderException
    {
        String hotTableTxt = hopTable.getText();
        if(!isIntegerArray(hotTableTxt.split(",")))
        {
            throw new ReaderException("Hoptable values was either too large or too small for int");
        }
        int hopTable[] = stringArray2IntArray(hotTableTxt.split(","));
        r.paramSet("/reader/region/hopTable", hopTable);
    }

    private int[] stringArray2IntArray(String[] values){
        try{
            if(values != null && values.length > 0){
                int[] num = new int[values.length];
                for(int i = 0; i <= values.length-1; i++ ){
                    num[i] = Integer.parseInt(values[i].trim());
                }
                return num;
            }
        }catch(Exception e){
           
        }
        return null;
    }
    
    public void setTitledPanesStatus(boolean rw, boolean pt, boolean rt, boolean dp, boolean pf, boolean fu, boolean abt)
    {
        Platform.runLater(new Runnable() 
        {
            @Override
            public void run() 
            {
                readWriteTitledPane.setDisable(rw);
                performanceTuningTitlePane.setDisable(pt);
                regulatoryTestingPane.setDisable(rt);
                displayOptionsTitlePane.setDisable(dp);
                profileTitlePane.setDisable(pf);
                firmwareUpdateTitledPane.setDisable(fu);
                aboutTitledPane.setDisable(abt);
            }
        });
    }

    public void setTitledPanesExpandedStatus(boolean rw, boolean pt, boolean rt, boolean dp, boolean pf, boolean fu, boolean abt)
    {
        Platform.runLater(new Runnable() 
        {
            @Override
            public void run() 
            {
                readWriteTitledPane.setExpanded(rw);
                performanceTuningTitlePane.setExpanded(pt);
                regulatoryTestingPane.setExpanded(rt);
                displayOptionsTitlePane.setExpanded(dp);
                profileTitlePane.setExpanded(pf);
                firmwareUpdateTitledPane.setExpanded(fu);
                aboutTitledPane.setExpanded(abt);
            }
        });
    }

    @FXML
    private void loadConfig()
    {
        try
        {
            ReadExceptionListener loadExceptionListener = new LoadSaveExceptionReciver();
            r.addReadExceptionListener(loadExceptionListener);
            fileFilters = new ArrayList<String>();
            fileFilters.add("*.urac");
            chooseFile("Choose configuration file", "Configuration file (*.urac)", fileFilters);
            File configFile = fileChooser.showOpenDialog(stage);

            if (configFile != null)
            {
                isLoadSaveCompleted = false;
                final String path  = configFile.getAbsolutePath();

                new Thread(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                        try
                        {
                            if(loadSaveError == null)
                            {
                                loadSaveError = new StringBuffer();
                            }
                            loadSaveProperties = new Properties();
                            FileInputStream fis =  new FileInputStream(configFile);
                            loadSaveProperties.load(fis);
                            fis.close();

                            Platform.runLater(new Runnable()
                            {
                                @Override
                                public void run() 
                                {
                                    try
                                    {
                                        loadConfigurations();
                                        enableDisableGen2Settings(new ActionEvent());
                                        isLoadSaveCompleted = true;
                                    }    
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                        isLoadSaveCompleted = true;
                                    }
                                }
                            });
                            while(!isLoadSaveCompleted)
                            {
                                // do nothing wait for untill load save completes
                            }    
                            r.removeReadExceptionListener(loadExceptionListener);
                            if(loadSaveError != null && !loadSaveError.toString().equals(""))
                            {
                                JOptionPane.showMessageDialog(null, loadSaveError , "Load Save Errors", JOptionPane.ERROR_MESSAGE);
                                loadSaveError = null;
                            }

                            int option = JOptionPane.showConfirmDialog(null, "Applying configurations along with autonomous read will navigate to tag results.", "Confirmation", JOptionPane.YES_NO_OPTION);
                            if (option == JOptionPane.YES_OPTION)
                            {
                                applyConfigurationsToModule();
                            }
                        }
                        catch(Exception e)
                        {
                            //e.printStackTrace();
                            loadSaveError = null;
                        }
                    }
                }).start();
            }
            isLoadSaveConfiguration = false;
        } 
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            loadSaveError = null;
        }
    }

    public void loadConfigurations()
    {
        if(loadSaveProperties != null)
        {
            Properties prop = loadSaveProperties;
            
            for(Object key: prop.keySet())
            {
                String param = key.toString();
                String value = prop.get(key).toString().trim();

                if(param.equalsIgnoreCase("/reader/region/id"))
                 {
                    Reader.Region[] supportedRegions  = null;
                    Reader.Region currentRegion       = null;
                    try 
                    {
                        supportedRegions = (Reader.Region[]) r.paramGet("/reader/region/supportedRegions");
                    } 
                    catch (Exception e) 
                    {

                    }
                    
                    if(null == value || value.equals(""))
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value "+value +" for "+ param
                                    + ". Application sets this parameter to previous set value. \n");
                    }
                    else
                    {
                        try
                        {
                            currentRegion = Reader.Region.valueOf(value);
                            r.paramSet("/reader/region/id", currentRegion);
                            region.getSelectionModel().select(currentRegion);
                        }
                        catch(Exception e)
                        {
                            loadSaveError.append("Invalid value " + value + " for " + param +". Please enter a supported region"
                            + " ACT gets and applies region "+currentRegion+" to ACT or change to the supported value and reload the configuration. \n");
                        }
                    }
                }

                else if(param.equalsIgnoreCase("/reader/baudRate"))
                {
                    if(probeBaudRate.getItems().contains(value))
                    {
                        probeBaudRate.getSelectionModel().select(value);
                    }
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value "+value +" for "+ param
                                    + ". Application sets this parameter to previous set value. \n");
                    }
                }

                else if(param.equalsIgnoreCase("/reader/radio/readPower"))
                {
                   try
                   {
                      Float power = Float.parseFloat(value);
                      if(power >= minReaderPower && power <= maxReaderPower)
                      {
                          rfRead.setText(String.valueOf(power / 100));                          
                          powerEventListener(readPowerSlider, writePowerSlider, rfRead, rfWrite);
                          readPowerChanged();
                      }
                      else
                      {
                         value = value.isEmpty() ? "empty" : value; 
                         loadSaveError.append("Read power "+ param +" value "+ value
                               + " setting failed.  Please enter within "+minReaderPower+" and  "+maxReaderPower
                         + " in Load/Save file. Application sets this parameter to previous set value. \n");
                      }
                   }
                   catch(Exception ex)
                   {
                       loadSaveError.append("Invalid value "+ value +" for "+ param
                               + ". Application sets this parameter to previous set value. \n");
                   }
                }
                else if(param.equalsIgnoreCase("/reader/radio/writePower"))
                {
                   try
                   {
                      Float power = Float.parseFloat(value);
                      if(power >= minReaderPower && power <= maxReaderPower)
                      {
                          rfWrite.setText(String.valueOf(power / 100));
                          powerEventListener(writePowerSlider,readPowerSlider ,rfWrite,rfRead);
                          writePowerChanged();
                      }
                      else
                      {
                         value = value.isEmpty() ? "empty" : value; 
                         loadSaveError.append("Write power "+ param +" value "+ value
                               + " setting failed.  Please enter within "+minReaderPower+" and  "+maxReaderPower
                         + " in Load/Save file. Application sets this parameter to previous set value. \n");
                      }
                   }
                   catch(Exception ex)
                   {
                       loadSaveError.append("Invalid value "+ value +" for "+ param
                               + ". Application sets this parameter to previous set value. \n");
                   }
                }
                else if(param.equalsIgnoreCase("/reader/gen2/BLF"))
                {
                    if(value.equalsIgnoreCase("LINK640KHZ"))
                    {
                        if (false
                                || readerModel.equalsIgnoreCase("M5e")
                                || readerModel.equalsIgnoreCase("M5e PRC")
                                || readerModel.equalsIgnoreCase("M5e EU"))
                        {
                            loadSaveError.append("Value "+value +" not supported on this reader."
                                    + ". Application skips this parameter. \n");
                        }
                        else
                        {
                          String tagEncoding = prop.getProperty("/reader/gen2/tagEncoding");
                          if(false
                                  || tagEncoding.equalsIgnoreCase("M2")
                                  || tagEncoding.equalsIgnoreCase("M4")
                                  || tagEncoding.equalsIgnoreCase("M8"))
                          {
                            continue;
                          }
                          link640Khz.selectedProperty().setValue(true);
                        }
                    }
                    else if(value.equalsIgnoreCase("LINK250KHZ"))
                    {
                        link250Khz.selectedProperty().setValue(true);
                    }
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value "+value +" for "+ param
                                    + ". Application sets this parameter to previous set value. \n");
                    }
                }
                else if(param.equalsIgnoreCase("/reader/gen2/tari"))
                {
                    String linkFreq = prop.getProperty("/reader/gen2/BLF");
                    value = value.isEmpty() ? "empty" : value;
                    if(linkFreq.equalsIgnoreCase("LINK640KHZ") && 
                            (false 
                            || value.equalsIgnoreCase("TARI_12_5US")
                            || value.equalsIgnoreCase("TARI_25US")))
                    {
                        loadSaveError.append("Invalid tagencoding value "+ value +" for the given link frequency "+linkFreq
                        +". Apllication skips this parameter and set to previous set value. \n");
                        continue;
                    }
                    else if (value.equalsIgnoreCase("TARI_6_25US"))
                    {
                       tari6_25us.selectedProperty().setValue(true); 
                    }
                    else if (value.equalsIgnoreCase("TARI_12_5US"))
                    {
                       tari12_5us.selectedProperty().setValue(true);
                    }
                    else if(value.equalsIgnoreCase("TARI_25US"))
                    {
                       tari25us.selectedProperty().setValue(true);
                    }
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value "+value +" for "+ param
                                    + ". Application sets this parameter to previous set value. \n");
                    }
                }
                else if(param.equalsIgnoreCase("/reader/gen2/tagEncoding"))
                {
                    String linkFreq = prop.getProperty("/reader/gen2/BLF");
                    value = value.isEmpty() ? "empty" : value;
                    if(linkFreq.equalsIgnoreCase("LINK640KHZ") && 
                            (false 
                            || value.equalsIgnoreCase("M2")
                            || value.equalsIgnoreCase("M4")
                            || value.equalsIgnoreCase("M8")))
                    {
                        loadSaveError.append("Invalid tagencoding value "+ value +" for the given link frequency "+linkFreq
                        +". Apllication skips this parameter and set to previous set value. \n");
                        continue;
                    }
                    
                    if(value.equalsIgnoreCase("FM0"))
                    {
                       fm0.selectedProperty().setValue(true);
                    }
                    else if (value.equalsIgnoreCase("M2"))
                    {
                       m2.selectedProperty().setValue(true);
                    }
                    else if (value.equalsIgnoreCase("M4"))
                    {
                       m4.selectedProperty().setValue(true); 
                    }
                    else if (value.equalsIgnoreCase("M8"))
                    {
                       m8.selectedProperty().setValue(true);
                    }
                    else
                    {
                      loadSaveError.append("Invalid value "+value +" for "+ param
                                    + ". Application sets this parameter to previous set value. \n");
                    }
                }
                else if(param.equalsIgnoreCase("/reader/gen2/session"))
                {
                    if(value.equalsIgnoreCase("S0"))
                    {
                       sessionS0.selectedProperty().setValue(true); 
                    }
                    else if (value.equalsIgnoreCase("S1"))
                    {
                       sessionS1.selectedProperty().setValue(true); 
                    }
                    else if (value.equalsIgnoreCase("S2"))
                    {
                       sessionS2.selectedProperty().setValue(true); 
                    }
                    else if (value.equalsIgnoreCase("S3"))
                    {
                        sessionS3.selectedProperty().setValue(true);
                    }
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value "+value +" for "+ param
                                    + ". Application sets this parameter to previous set value. \n");
                    }
                }
                else if(param.equalsIgnoreCase("/reader/gen2/target"))
                {
                    if(value.equalsIgnoreCase("A"))
                    {
                       targetA.selectedProperty().setValue(true);
                    }
                    else if (value.equalsIgnoreCase("AB"))
                    {
                       targetAB.selectedProperty().setValue(true); 
                    }
                    else if (value.equalsIgnoreCase("B"))
                    {
                       targetB.selectedProperty().setValue(true);
                    }
                    else if (value.equalsIgnoreCase("BA"))
                    {
                       targetBA.selectedProperty().setValue(true);
                    }
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value "+value +" for "+ param
                                    + ". Application sets this parameter to previous set value. \n");
                    }
                    gen2TargetChangeConfiguration();
                }
                else if(param.equalsIgnoreCase("/reader/gen2/q"))
                {
                    if(value != null && !value.isEmpty() && value.equalsIgnoreCase("StaticQ"))
                    {
                       staticQ.selectedProperty().setValue(true);
                       String qText = prop.getProperty("/application/performanceTuning/staticQValue");
                        if(!isInteger(qText))
                        {
                            loadSaveError.append("Invalid value "+qText +" for /application/performanceTuning/staticQValue"
                                    + ". Application sets this parameter to previous set value. \n");
                        }
                        else
                        {
                            int qValue = Integer.parseInt(qText);
                            if(qValue >= 0 && qValue <= 15)
                            {
                                staticQList.getSelectionModel().select(qValue);
                            }
                            else
                            {
                                loadSaveError.append("Invalid value "+qText+" for /application/performanceTuning/staticQValue value within 0 to 15"
                                    + ". Application sets this parameter to previous set value. \n");
                            }
                        }
                    }
                    else if (value != null && !value.isEmpty() && value.equalsIgnoreCase("DynamicQ"))
                    {
                       dynamicQ.selectedProperty().setValue(true); 
                    }
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value "+value +" for "+ param
                                    + ". Application sets this parameter to previous set value. \n");
                    }
                    gen2Q(new ActionEvent());
                }

                else if(param.equalsIgnoreCase("/reader/region/hopTime"))
                {
                    if (value != null && !value.isEmpty() && isInteger(value))
                    {
                        hopTime.setText(value);
                        try
                        {
                            setHopTime();
                        }
                        catch(Exception e)
                        {
                            value = value.isEmpty() ? "empty" : value;
                            loadSaveError.append("Invalid value "+ value +" for "+ param+". "+e.getMessage()
                             +". Application sets this parameter based on region value. \n");
                            try
                            {
                                setHopTimeToUI();
                            }
                            catch(Exception ex)
                            {

                            }
                        }
                    }
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value "+ value +" for "+ param+"."
                         + " This parameter accepts only integer. Application sets this parameter based on region value. \n");
                    }
                }

                else if(param.equalsIgnoreCase("/reader/region/hopTable"))
                {
                    if (value != null && !value.isEmpty() && isIntegerArray(value.substring(1, value.length()-1).split(",")))
                    {
                        hopTable.setText(parseHopTableValues(stringArray2IntArray(value.substring(1, value.length()-1).split(","))));
                        try
                        {
                            setHopTable();
                        }
                        catch(Exception e)
                        {
                            value = value.isEmpty() ? "empty" : value;
                            loadSaveError.append("Invalid value \n"+ value +"\n for "+ param+". "+e.getMessage()
                             +". Application sets this parameter based on region value. \n");
                            try
                            {
                                setHopTableToUI();
                            }
                            catch(Exception ex)
                            {

                            }
                        }
                    }
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value "+ value +" for "+ param+"."
                         + " This parameter accepts only Ineteger Array. Application sets this parameter based on region value. \n");
                    }
                }

                else if(param.equalsIgnoreCase("/reader/antenna/checkPort"))
                {
                    isLoadSaveConfiguration = true;
                    if (value.equalsIgnoreCase("true"))
                    {
                        try
                        {
                           antennaDetection.setSelected(true);
                           findAntennas(new ActionEvent());
                        }
                        catch(Exception ex)
                        {
                            
                        }
                    }
                    else if (value.equalsIgnoreCase("false"))
                    {
                        antennaDetection.selectedProperty().setValue(false);
                    }
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value "+ value +" for "+ param+"."
                         + " This parameter accepts only true or false. Application sets this parameter to previous set value. \n");
                    }
                }

                else if(param.equalsIgnoreCase("/application/readwriteOption/Protocols"))
                {
                    String[] protocols = value.split(",");
                    for(String proto : protocols)
                    {
                        if(!supportedProtocols.contains(proto.toUpperCase()))
                        {
                            proto = proto.isEmpty() ? "empty" : proto;
                            loadSaveError.append(proto + " is not supported protocol."
                                    + " Application skips this parameter "+ param + " and sets to previous set value. \n");
                            continue;
                        }
                        if(proto.equalsIgnoreCase("gen2"))
                        {
                            gen2.selectedProperty().setValue(true);
                        }
                        else if(proto.equalsIgnoreCase("ISO18000-6B"))
                        {
                            iso18000.selectedProperty().setValue(true);
                        }
                        else if(proto.equalsIgnoreCase("ipx64"))
                        {
                            ipx64.selectedProperty().setValue(true);
                        }
                        else if(proto.equalsIgnoreCase("ipx256"))
                        {
                            ipx256.selectedProperty().setValue(true);
                        }
                        else
                        {
                            loadSaveError.append(proto + " is not valid protocol option for "+ param + "."
                                    + " Application skips this protocol \n");
                        }
                    }
                }

                else if(param.equalsIgnoreCase("/application/readwriteOption/Antennas"))
                {
                    if(!antennaDetection.isSelected())
                    {    
                        String[] antennaValue = value.split(",");
                        antenna1.setDisable(false);
                        antenna2.setDisable(false);
                        antenna3.setDisable(false);
                        antenna4.setDisable(false);
                        antenna1.selectedProperty().setValue(false);
                        antenna2.selectedProperty().setValue(false);
                        antenna3.selectedProperty().setValue(false);
                        antenna4.selectedProperty().setValue(false);
                        for (String ant : antennaValue)
                        {
                            try
                            {
                                int antenna = Integer.parseInt(ant);
                                if (!existingAntennas.contains(antenna))
                                {
                                    ant = ant.isEmpty() ? "empty" : ant;
                                    loadSaveError.append("Antenna " + ant + " is not supported on this reader."
                                            + " Application skips this antenna. \n");    
                                    continue;
                                }
                            }
                            catch(Exception ex)
                            {
                               ant = ant.isEmpty() ? "empty" : ant; 
                               loadSaveError.append("Invalid value "+ ant+"  for parameter "+ param + ". Application skips this parameter "
                                       +" and sets to previous set value. \n");
                               continue;
                            }
                            if (ant.equalsIgnoreCase("1"))
                            {
                               antenna1.selectedProperty().setValue(true);
                            } 
                            else if (ant.equalsIgnoreCase("2"))
                            {
                               antenna2.selectedProperty().setValue(true);
                            }
                            else if (ant.equalsIgnoreCase("3"))
                            {
                                antenna3.selectedProperty().setValue(true);
                            }
                            else if (ant.equalsIgnoreCase("4"))
                            {
                                antenna4.selectedProperty().setValue(true);
                            }
                            else
                            {
                              loadSaveError.append("Invalid antenna value. Supported antennas are "+ existingAntennas.toString()
                              + " Application skips "+param+" parameter. \n");
                            }
                        }
                    }
                }
                else if(param.equalsIgnoreCase("/application/readwriteOption/enableAutonomousRead"))
                {
                    if(autonomousRead.isDisabled())
                    {
                        loadSaveError.append("Autonomous read is not supported on this reader. "
                                + " Application skips "+param+" parameter. \n");
                    }
                    else
                    {
                        if(value.equalsIgnoreCase("true"))
                        {
                           autonomousRead.selectedProperty().setValue(true);
                        }
                        else if(value.equalsIgnoreCase("false"))
                        {
                           autonomousRead.selectedProperty().setValue(false); 
                        }
                        else
                        {
                            value = value.isEmpty() ? "empty" : value;
                            loadSaveError.append("Invalid value " + value + " for " + param + "."
                                    + " This parameter accepts only true or false. Application sets this parameter to previous set value. \n");
                        }
                    }
                    autonomousReadChangeConfiguration(new ActionEvent());
                }
                else if(param.equalsIgnoreCase("/application/readwriteOption/enableAutoReadGPI"))
                {
                    if(gpiTriggerRead.isDisabled())
                    {
                        loadSaveError.append("GPI trigger read is not supported on this reader. "
                                + " Application skips "+param+" parameter. \n");
                    }
                    else
                    {
                        if(value.equalsIgnoreCase("true"))
                        {
                            gpiTriggerRead.selectedProperty().setValue(true);
                            String pin = prop.get("/application/readwriteOption/autoReadgpiPin").toString();
                            if (pin.equalsIgnoreCase("1"))
                            {
                                autoReadGpiGroup.selectToggle(autoReadGpi1);
                            }
                            else if (pin.equalsIgnoreCase("2"))
                            {
                                autoReadGpiGroup.selectToggle(autoReadGpi2);
                            }
                            else if (pin.equalsIgnoreCase("3")  && !(readerModel.equalsIgnoreCase("M6e Micro")||readerModel.equalsIgnoreCase("M6e Micro USBPro")))
                            {
                                autoReadGpiGroup.selectToggle(autoReadGpi3);
                            }
                            else if (pin.equalsIgnoreCase("4") && !(readerModel.equalsIgnoreCase("M6e Micro")||readerModel.equalsIgnoreCase("M6e Micro USBPro")))
                            {
                                autoReadGpiGroup.selectToggle(autoReadGpi4);
                            }
                            else
                            {
                                pin = pin.isEmpty() ? "empty" : pin;
                                loadSaveError.append("Invalid GPI pin value "+ pin +" for parameter /application/readwriteOption/autoReadgpiPin."
                                        + " Applicaion sets this parameter to previous set value. \n");
                            }
                        }
                        else if(value.equalsIgnoreCase("false"))
                        {
                           gpiTriggerRead.selectedProperty().setValue(false); 
                        }
                        else
                        {
                            value = value.isEmpty() ? "empty" : value;
                            loadSaveError.append("Invalid value " + value + " for " + param + "."
                                    + " This parameter accepts only true or false. Application sets this parameter to previous set value. \n");
                        }
                    }
                }

                else if(param.equalsIgnoreCase("/application/displayOption/tagResultColumnSelection/enableAntenna"))
                {
                    if (value.equalsIgnoreCase("true"))
                    {
                        metaDataAntenna.selectedProperty().setValue(true);
                    } 
                    else if (value.equalsIgnoreCase("false"))
                    {
                        metaDataAntenna.selectedProperty().setValue(false);
                    } 
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value " + value + " for " + param + "."
                                + " This parameter accepts only true or false. Application sets this parameter to previous set value. \n");
                    }
                }
                else if(param.equalsIgnoreCase("/application/displayOption/tagResultColumnSelection/enableProtocol"))
                {
                    if (value.equalsIgnoreCase("true"))
                    {
                        metaDataProtocol.selectedProperty().setValue(true);
                    } 
                    else if (value.equalsIgnoreCase("false"))
                    {
                        metaDataProtocol.selectedProperty().setValue(false);
                    } 
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value " + value + " for " + param + "."
                                + " This parameter accepts only true or false. Application sets this parameter to previous set value. \n");
                    }
                }
                else if(param.equalsIgnoreCase("/application/displayOption/tagResultColumnSelection/enableFrequency"))
                {
                    if (value.equalsIgnoreCase("true"))
                    {
                        metaDataFrequency.selectedProperty().setValue(true);
                    } 
                    else if (value.equalsIgnoreCase("false"))
                    {
                        metaDataFrequency.selectedProperty().setValue(false);
                    } 
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value " + value + " for " + param + "."
                                + " This parameter accepts only true or false. Application sets this parameter to previous set value. \n");
                    }
                }
                else if(param.equalsIgnoreCase("/application/displayOption/tagResultColumnSelection/enablePhase"))
                {
                    if (value.equalsIgnoreCase("true"))
                    {
                        metaDataPhase.selectedProperty().setValue(true);
                    } 
                    else if (value.equalsIgnoreCase("false"))
                    {
                        metaDataPhase.selectedProperty().setValue(false);
                    } 
                    else
                    {
                        loadSaveError.append("Invalid value " + value + " for " + param + "."
                                + " This parameter accepts only true or false. Application sets this parameter to previous set value. \n");
                    }
                }
                else if(param.equalsIgnoreCase("/application/readwriteOption/enableEmbeddedReadData"))
                {
                    if (value.equalsIgnoreCase("true"))
                    {
                        embeddedReadEnable.selectedProperty().setValue(true);
                        String isUniqByData = prop.getProperty("/reader/tagReadData/uniqueByData").trim();
                        if (isUniqByData.equalsIgnoreCase("true")) 
                        {
                            embeddedReadUnique.selectedProperty().setValue(true);
                        } 
                        else if (isUniqByData.equalsIgnoreCase("false")) 
                        {
                            embeddedReadUnique.selectedProperty().setValue(false);
                        } 
                        else
                        {
                            isUniqByData = isUniqByData.isEmpty() ? "empty" : isUniqByData;
                            loadSaveError.append("Invalid value " + isUniqByData + " for /reader/tagReadData/uniqueByData."
                                    + " This parameter accepts only true or false. Application sets this parameter to previous set value. \n");
                        }
                        String memBank = prop.getProperty("/application/readwriteOption/enableEmbeddedReadData/MemBank").trim();
                        if(memBank.equalsIgnoreCase("EPC"))
                        {
                            embeddedMemoryBank.getSelectionModel().select("EPC");
                        }
                        else if(memBank.equalsIgnoreCase("RESERVED"))
                        {
                            embeddedMemoryBank.getSelectionModel().select("RESERVED"); 
                        }
                        else if(memBank.equalsIgnoreCase("TID"))
                        {
                           embeddedMemoryBank.getSelectionModel().select("TID");
                        }
                        else if(memBank.equalsIgnoreCase("USER"))
                        {
                           embeddedMemoryBank.getSelectionModel().select("USER");
                        }
                        else
                        {
                          memBank = memBank.isEmpty() ? "empty" : memBank;  
                          loadSaveError.append("Invalid memory bank " + memBank + " for /application/readwriteOption/enableEmbeddedReadData/MemBank."
                                    + " This parameter accepts only tag memory banks. Application sets this parameter to previous set value. \n");
                        }
                        
                       String strtAddr = prop.getProperty("/application/readwriteOption/enableEmbeddedReadData/StartAddress").trim();
                       try
                       {
                           changeListMap.put("embeddedStart", embeddedStart.getText());
                           int addr = Integer.parseInt(strtAddr);
                           embeddedStart.setText(strtAddr);
//                           addChangeList("Embedded Start previous value :" + changeListMap.get("embeddedStart") + " Now: " + embeddedStart.getText());
                           changeListMap.put("embeddedStart", embeddedStart.getText());
                       }
                       catch(Exception ex)
                       {
                           strtAddr = strtAddr.isEmpty() ? "empty" : strtAddr;
                            loadSaveError.append("Invalid start address  " + strtAddr + " for /application/readwriteOption/enableEmbeddedReadData/StartAddress."
                                    + " This parameter accepts only integer value. Application sets this parameter to previous set value. \n");
                       }
                       String endAddr = prop.getProperty("/application/readwriteOption/enableEmbeddedReadData/NoOfWordsToRead").trim();
                       try
                       {
                           changeListMap.put("embeddedEnd", embeddedEnd.getText());
                           int addr = Integer.parseInt(strtAddr);
                           embeddedEnd.setText(endAddr);
//                           addChangeList("Embedded End previous value: " + changeListMap.get("embeddedEnd") + " Now: " + embeddedEnd.getText());
                           changeListMap.put("embeddedEnd", embeddedEnd.getText());
                       }
                       catch(Exception ex)
                       {
                         endAddr = endAddr.isEmpty() ? "empty" : endAddr;
                         loadSaveError.append("Invalid value for length  " + endAddr + " for /application/readwriteOption/enableEmbeddedReadData/NoOfWordsToRead."
                                    + " This parameter accepts only integer value. Application sets this parameter to previous set value. \n");
                       }
                    }
                    else if (value.equalsIgnoreCase("false"))
                    {
                        embeddedReadEnable.selectedProperty().setValue(false);
                    } 
                    else
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value " + value + " for " + param + "."
                                + " This parameter accepts only true or false. Application sets this parameter to previous set value. \n");
                    }
                }
                else if(param.equalsIgnoreCase("/reader/read/asyncOnTime"))
                {
                    try
                    {
                        int asyncOnTimeValue = Integer.parseInt(value);
                        if(asyncOnTimeValue >= 0)
                        {
                            dutyCycleOn.setText(String.valueOf(asyncOnTimeValue));
                            validateTextFields();
                        }
                    }
                    catch(Exception e)
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value " + value + " for " + param + "."
                                + "Application sets this parameter to previous set value. \n");
                    }
                }
                else if(param.equalsIgnoreCase("/reader/read/asyncOffTime"))
                {
                    try
                    {
                        int asyncOffTimeValue = Integer.parseInt(value);
                        if(asyncOffTimeValue >= 0)
                        {
                            dutyCycleOff.setText(String.valueOf(asyncOffTimeValue));
                            validateTextFields();
                        }
                    }
                    catch(Exception e)
                    {
                        value = value.isEmpty() ? "empty" : value;
                        loadSaveError.append("Invalid value " + value + " for " + param + "."
                                + "Application sets this parameter to previous set value. \n");
                    }
                }
            }
            if((prop.getProperty("/application/readwriteOption/enableAutoReadGPI").toString().equalsIgnoreCase("true"))
                    && (prop.getProperty("/application/readwriteOption/enableAutonomousRead").toString().equalsIgnoreCase("true")))
            {
                loadSaveError.append("Params /application/readwriteOption/enableAutoReadGPI and "
                        + "application/readwriteOption/enableAutonomousRead can't be enabled at the same time. "
                        + "Please select either one of them. Skipping these params");
            }
            loadSaveError.trimToSize();
        }
    }
    
    void notifyLoadSaveException(String message)
    {
        showWarningErrorMessage("warning", message);
    }
    
    @FXML
    private void saveConfig()
    {
        if(fileFilters == null)
        {
            fileFilters = new ArrayList<String>();
            fileFilters.add("*.urac");
        }
        if(loadSave == null)
        {
            loadSave = new LoadSaveConfig();
        }
        chooseFile("Select a configuration file to save reader UI configuration parameters", ".urac", fileFilters);
        String loadFileName = readerModel+"_"+ deviceName;
        fileChooser.setInitialFileName(loadFileName.replace("/", ""));
        File configFile = fileChooser.showSaveDialog(stage);
        if(configFile != null)
        {
            getParametersToSave();
            loadSave.saveConfigurations(configFile.getAbsolutePath(), saveParams);
            showWarningErrorMessage("success", "Saved reader and UI configuration parameters successfully.");
        }
    }
    
    public void getParametersToSave()
    {
        //save Region configuration
        StringBuilder sb = new StringBuilder();
        if(region.getSelectionModel().getSelectedItem() == null || "".equals(region.getSelectionModel().getSelectedItem()))
        {
            sb.append("");
        }
        else
        {
            sb.append(region.getSelectionModel().getSelectedItem().toString());
        }
        saveParams.put("/reader/region/id", sb.toString());
        
        //save Region configuration
        sb = new StringBuilder();
        if(probeBaudRate.getSelectionModel().getSelectedItem() != null || !("".equals(probeBaudRate.getSelectionModel().getSelectedItem().toString())))
        {
            sb.append(probeBaudRate.getSelectionModel().getSelectedItem().toString());
        }
        saveParams.put("/reader/baudRate", sb.toString());

        //save protocol configuration
        sb = new StringBuilder();
        if(gen2.isSelected())
        {
            sb.append(gen2.getText());
            sb.append(",");
        }
        if(iso18000.isSelected())
        {
            sb.append(iso18000.getText());
            sb.append(",");
        }
        if(ipx64.isSelected())
        {
            sb.append(ipx64.getText());
            sb.append(",");
        }
        if(ipx256.isSelected())
        {
            sb.append(ipx256.getText());
            sb.append(",");
        }
       
        if (sb.length() > 0)
        {
            sb.deleteCharAt(sb.length() - 1);
        }
      
        saveParams.put("/application/readwriteOption/Protocols", sb.toString());
        
        //save antenna configuration
         sb = new StringBuilder();
         if(antenna1.isSelected())
         {
             sb.append(antenna1.getText());
             sb.append(",");
         }
         if(antenna2.isSelected())
         {
             sb.append(antenna2.getText());
             sb.append(",");
         }
         if(antenna3.isSelected())
         {
             sb.append(antenna3.getText());
             sb.append(",");
         }
         if(antenna4.isSelected())
         {
             sb.append(antenna4.getText());
             sb.append(",");
         }
        
         if (sb.length() > 0)
        {
            sb.deleteCharAt(sb.length() - 1);
        }
      
        saveParams.put("/application/readwriteOption/Antennas", sb.toString());
        
        //antenna detection 
        if(antennaDetection.isDisabled())
        {
            // If the connected reader doesn't support antenna detection
             saveParams.put("/reader/antenna/checkPort", "false");
        }
        else
        {
           if(antennaDetection.isSelected())
           {
               saveParams.put("/reader/antenna/checkPort", "true");
           }
           else
           {
               saveParams.put("/reader/antenna/checkPort", "false");
           }
        }
        
        //Autonomous read
        if (autonomousRead.isSelected())
        {
            saveParams.put("/application/readwriteOption/enableAutonomousRead", "true");
        } 
        else
        {
            saveParams.put("/application/readwriteOption/enableAutonomousRead", "false");
        }

        //Gpi trigger read
        if (gpiTriggerRead.isSelected())
        {
            saveParams.put("/application/readwriteOption/enableAutoReadGPI", "true");
        } 
        else
        {
            saveParams.put("/application/readwriteOption/enableAutoReadGPI", "false");
        }
      
        saveParams.put("/application/readwriteOption/autoReadgpiPin", autoReadGpiGroup.getSelectedToggle().getUserData().toString());
      
        //Embeded read data
        if(embeddedReadEnable.isSelected())
        {
            saveParams.put("/application/readwriteOption/enableEmbeddedReadData", "true");
          
            // MemBank
            saveParams.put("/application/readwriteOption/enableEmbeddedReadData/MemBank",
                    embeddedMemoryBank.getSelectionModel().getSelectedItem().toString());

            // Start address
            saveParams.put("/application/readwriteOption/enableEmbeddedReadData/StartAddress", 
                    embeddedStart.getText());

            // Number of words to read
            saveParams.put("/application/readwriteOption/enableEmbeddedReadData/NoOfWordsToRead",
                    embeddedEnd.getText());
            
            // UniqueByData
            if(embeddedReadUnique.isSelected())
            {
                saveParams.put("/reader/tagReadData/uniqueByData","true");
            }
            else
            {
                saveParams.put("/reader/tagReadData/uniqueByData","false");
            }
        }
        else
        {
            saveParams.put("/application/readwriteOption/enableEmbeddedReadData", "false");
            
            // MemBank
            if(embeddedMemoryBank.getSelectionModel().getSelectedItem() == null)
            {
                saveParams.put("/application/readwriteOption/enableEmbeddedReadData/MemBank","");
            }
            else
            {    
                saveParams.put("/application/readwriteOption/enableEmbeddedReadData/MemBank", embeddedMemoryBank.getSelectionModel().getSelectedItem().toString());
            }

            // Start address
            saveParams.put("/application/readwriteOption/enableEmbeddedReadData/StartAddress", 
                    "0");

            // Number of words to read
            saveParams.put("/application/readwriteOption/enableEmbeddedReadData/NoOfWordsToRead",
                    "0");
            
            //UniqueByData
            saveParams.put("/reader/tagReadData/uniqueByData","false");
        }

        //read power
        saveParams.put("/reader/radio/readPower",String.valueOf((int)(Double.parseDouble(rfRead.getText()) * 100)));

        //write power
        saveParams.put("/reader/radio/writePower",String.valueOf((int)(Double.parseDouble(rfWrite.getText()) * 100)));

        //Gen2 settings
        saveParams.put("/reader/gen2/BLF", linkFreqGroup.getSelectedToggle().getUserData().toString());
        saveParams.put("/reader/gen2/tari", tariGroup.getSelectedToggle().getUserData().toString());
        saveParams.put("/reader/gen2/tagEncoding", tagEncodeGroup.getSelectedToggle().getUserData().toString());
        saveParams.put("/reader/gen2/session", sessionGroup.getSelectedToggle().getUserData().toString());
        saveParams.put("/reader/gen2/target", targetGroup.getSelectedToggle().getUserData().toString());

        if(dynamicQ.isSelected()){
            saveParams.put("/reader/gen2/q", "DynamicQ");
        }else{
            saveParams.put("/reader/gen2/q", "StaticQ");
            saveParams.put("/application/performanceTuning/staticQValue", staticQList.getSelectionModel().getSelectedItem().toString());
        }

        // Regulatory Testing
        saveParams.put("/reader/region/hopTime", hopTime.getText());
        saveParams.put("/reader/region/hopTable", Arrays.toString(hopTable.getText().split(",")).replaceAll("\\s+", ""));

        // Tag result column selection
        if(metaDataAntenna.isSelected())
        {
           saveParams.put("/application/displayOption/tagResultColumnSelection/enableAntenna", "true");
        }
        else
        {
           saveParams.put("/application/displayOption/tagResultColumnSelection/enableAntenna", "false"); 
        }
        
        if(metaDataProtocol.isSelected())
        {
           saveParams.put("/application/displayOption/tagResultColumnSelection/enableProtocol", "true");
        }
        else
        {
           saveParams.put("/application/displayOption/tagResultColumnSelection/enableProtocol", "false"); 
        }
        
        if(metaDataFrequency.isSelected())
        {
           saveParams.put("/application/displayOption/tagResultColumnSelection/enableFrequency", "true");
        }
        else
        {
           saveParams.put("/application/displayOption/tagResultColumnSelection/enableFrequency", "false"); 
        }
        
        if(metaDataPhase.isSelected())
        {
           saveParams.put("/application/displayOption/tagResultColumnSelection/enablePhase", "true");
        }
        else
        {
           saveParams.put("/application/displayOption/tagResultColumnSelection/enablePhase", "false"); 
        }
        //Async on time
        saveParams.put("/reader/read/asyncOnTime", dutyCycleOn.getText());
        
        //Async off time
        saveParams.put("/reader/read/asyncOffTime", dutyCycleOff.getText());
    }
    
    public void chooseFile(String title, String filterDescrition, List<String> filters)
    {
        fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filterDescrition, filters);
        fileChooser.getExtensionFilters().add(extFilter);
    }
    
    private Runnable dataPostThread = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                while(isReading)
                {
                    Iterator iterator = tagData.entrySet().iterator();
                    row = FXCollections.observableArrayList();
                    while (iterator.hasNext())
                    {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        String epc = (String) entry.getKey();
                        TagReadData tr = (TagReadData) tagData.get(epc);
                        if(embeddedReadEnable.isSelected())
                        {
                            row.add(new TagResults(deviceName,tr.epcString(),ReaderUtil.byteArrayToHexString(tr.data),
                                    ""+sdf.format(new Date(tr.readBase)),""+ tr.rssi,""+tr.readCount,""+ tr.antenna, ""+tr.readProtocol,""+ tr.frequency,""+ tr.phase));                            
                        }
                        else
                        {
                            row.add(new TagResults(deviceName,tr.epcString(),""+sdf.format(new Date(tr.readBase)),""+ 
                                    tr.rssi,""+tr.readCount,""+ tr.antenna, ""+tr.readProtocol,""+ tr.frequency,""+ tr.phase));
                        }
                    }
                    
                    if(!isConnected)
                    {
                        return;
                    }
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {  
                            if(!isReading)
                            {
                                return;
                            }
                            tableView.getItems().clear();
                            tableView.setItems(row);
                            uniqueTagCount.setText("" + uniqueTag);
                            totalTagCount.setText("" + totalTag);
                            temperature.setText(String.valueOf(statsTemperature));
                        }
                    });
                    Thread.sleep(200);
                }
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };
    
    public void tableViewConfiguration()
    {
        deviceIdColumn.setCellValueFactory(new PropertyValueFactory<TagResults, String>("deviceId"));
        epcColumn.setCellValueFactory(new PropertyValueFactory<TagResults, String>("epc"));
        timeStampColumn.setCellValueFactory(new PropertyValueFactory<TagResults, String>("time"));
        rssiColumn.setCellValueFactory(new PropertyValueFactory<TagResults, String>("rssi"));
        countColumn.setCellValueFactory(new PropertyValueFactory<TagResults, String>("count"));
        antennaColumn.setCellValueFactory(new PropertyValueFactory<TagResults, String>("antenna"));
        protocolColumn.setCellValueFactory(new PropertyValueFactory<TagResults, String>("protocol"));
        frequencyColumn.setCellValueFactory(new PropertyValueFactory<TagResults, String>("frequency"));
        phaseColumn.setCellValueFactory(new PropertyValueFactory<TagResults, String>("phase"));
        dataColumn.setCellValueFactory(new PropertyValueFactory<TagResults, String>("data"));
                                
        tableView.widthProperty().divide(4);
        deviceIdColumn.setVisible(false);
        antennaColumn.setVisible(false);
        protocolColumn.setVisible(false);
        frequencyColumn.setVisible(false);
        phaseColumn.setVisible(false);
        dataColumn.setVisible(false);

        metaDataAntenna.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
                public void changed(ObservableValue ov,Boolean old_val, Boolean new_val)
                {
                        if(metaDataAntenna.isSelected())
                        {
                            cloumnCount++;
                            antennaColumn.setVisible(true);
                            setNoColoumsInTable(cloumnCount);
                        }
                        else
                        {
                            cloumnCount--;
                            antennaColumn.setVisible(false);
                            setNoColoumsInTable(cloumnCount);
                        }
                }
            });
        metaDataProtocol.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
                public void changed(ObservableValue ov,Boolean old_val, Boolean new_val)
                {
                        if(metaDataProtocol.isSelected())
                        {
                            cloumnCount++;
                            protocolColumn.setVisible(true);
                            setNoColoumsInTable(cloumnCount);
                        }
                        else
                        {
                            cloumnCount--;
                            protocolColumn.setVisible(false);
                            setNoColoumsInTable(cloumnCount);
                        }
                }
            });
         metaDataFrequency.selectedProperty().addListener(new ChangeListener<Boolean>()
         {
                public void changed(ObservableValue ov,Boolean old_val, Boolean new_val) 
                {
                        if(metaDataFrequency.isSelected())
                        {
                            cloumnCount++;
                            frequencyColumn.setVisible(true);
                            setNoColoumsInTable(cloumnCount);
                        }
                        else
                        {
                            cloumnCount--;
                            frequencyColumn.setVisible(false);
                            setNoColoumsInTable(cloumnCount);
                        }
                }
            });
         metaDataPhase.selectedProperty().addListener(new ChangeListener<Boolean>()
         {
                public void changed(ObservableValue ov,Boolean old_val, Boolean new_val)
                {
                        if(metaDataPhase.isSelected())
                        {
                            cloumnCount++;
                            phaseColumn.setVisible(true);
                            setNoColoumsInTable(cloumnCount);
                        }
                        else
                        {
                            cloumnCount--;
                            phaseColumn.setVisible(false);
                            setNoColoumsInTable(cloumnCount);
                        }
                }
            });
         embeddedReadEnable.selectedProperty().addListener(new ChangeListener<Boolean>()
         {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (embeddedReadEnable.isSelected())
                {
                    cloumnCount++;
                    dataColumn.setVisible(true);
                    setNoColoumsInTable(cloumnCount);
                }
                else
                {
                    cloumnCount--;
                    dataColumn.setVisible(false);
                    setNoColoumsInTable(cloumnCount);
                }
            }
         
         });
    }
    
    @FXML
    private void loadFirmware(ActionEvent event) 
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose firmware");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Firmware (*.sim)", "*.sim");
        fileChooser.getExtensionFilters().add(extFilter);
        File firmware = fileChooser.showOpenDialog(stage);
        if(firmware != null)
        {
           firmwareFile = firmware.getAbsolutePath();
           selectedFilePath.setText(firmwareFile);
           updateFirmware.setDisable(false);
           updateFirmware.setStyle("-fx-background-color:#28B86D");
        }
    }

    @FXML
    private void updateFirmware(ActionEvent event)
    {
        try
        {
            updateFirmware.setDisable(true);
            updateFirmware.setStyle("-fx-background-color:#C6C6C6");
            loadFirmware.setOpacity(disableOpacity);
            loadFirmware.setStyle("-fx-background-color:#C6C6C6");
            loadFirmware.setDisable(true);
            loadConfigButton.setDisable(true);
            saveConfigButton.setDisable(true);
            fi = new FileInputStream(firmwareFile);
            progressBar.visibleProperty().setValue(true);
            progressTask = createProgress();
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(progressTask.progressProperty());
            applyButton.setDisable(true);
            revertButton.setDisable(true);
            connectTab.setDisable(true);
            btGetStarted.setDisable(true);
            new Thread(updateFirmwareThread).start();
        }
        catch(Exception ex)
        {
            showWarningErrorMessage("error", ex.getMessage());
            stopFirmwareProgress();
        }
    }

    private Runnable updateFirmwareThread = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                if(r != null)
                {
                    r.destroy();
                    r = null;
                }
                r = Reader.create("tmr:///" + deviceName);
                try
                {
                  r.connect();
                }
                catch(ReaderException re)
                {
                    if(re.getMessage().equalsIgnoreCase(Constants.APPLICATION_IMAGE_FAILED))
                    {
                        // do nothing when we got this error and update the firmware
                        revertButton.setDisable(true);
                    }
                    else
                    {
                        throw re;
                    }
                }
                r.firmwareLoad(fi);
                stopFirmwareProgress();
                fi.close();
                new Thread(disConnectThread).start();
                showWarningErrorMessage("success", "Firmware updated succesfully, please reconnect to the reader");
            }
            catch (Exception ex)
            {
                setTitledPanesStatus(false, false, false, false, false, false,false);
                setTitledPanesExpandedStatus(false, false, false, false, false, false, true);
                String errorMessage = ex.getMessage();
                if(ex instanceof IndexOutOfBoundsException)
                {
                   errorMessage = "Invalid firmware file.";
                   stopFirmwareProgress();
                }
                else if(errorMessage.equalsIgnoreCase("Firmware Update is successful. Autonomous mode is already enabled on reader"))
                {
                    try
                    {
                        stopFirmwareProgress();
                        fi.close();
                        new Thread(disConnectThread).start();
                    }
                    catch(Exception e)
                    {
                        
                    }
                    showWarningErrorMessage("warning", errorMessage);
                    return;
                }
                stopFirmwareProgress();
                showWarningErrorMessage("error", errorMessage);
            }
            return;
        }
    };
   
   public void stopFirmwareProgress()
    {
        Platform.runLater(new Runnable()
        {
            public void run()
            {
                loadFirmware.setDisable(false);
                loadFirmware.setOpacity(buttonEnableOpacity);
                loadFirmware.setStyle("-fx-background-color:#28B86D");
                progressTask.cancel(true);
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                progressBar.visibleProperty().setValue(false);
                selectedFilePath.clear();
                loadConfigButton.setDisable(false);
                saveConfigButton.setDisable(false);
                connectTab.setDisable(false);
                btGetStarted.setDisable(false);
            }
        });
    }
   
    public void setNoColoumsInTable(int cloumnCount)
    {
        epcColumn.prefWidthProperty().bind(tableView.widthProperty().divide(cloumnCount));
        timeStampColumn.prefWidthProperty().bind(tableView.widthProperty().divide(cloumnCount));
        rssiColumn.prefWidthProperty().bind(tableView.widthProperty().divide(cloumnCount));
        countColumn.prefWidthProperty().bind(tableView.widthProperty().divide(cloumnCount)); 
        deviceIdColumn.prefWidthProperty().bind(tableView.widthProperty().divide(cloumnCount));
        
        if(metaDataAntenna.isSelected())
        {
            antennaColumn.prefWidthProperty().bind(tableView.widthProperty().divide(cloumnCount));
        }
        if(metaDataProtocol.isSelected())
        {
            protocolColumn.prefWidthProperty().bind(tableView.widthProperty().divide(cloumnCount));
        }
        if(metaDataFrequency.isSelected())
        {
            frequencyColumn.prefWidthProperty().bind(tableView.widthProperty().divide(cloumnCount));
        }
        if(metaDataPhase.isSelected())
        {
            phaseColumn.prefWidthProperty().bind(tableView.widthProperty().divide(cloumnCount));
        }        
        if(embeddedReadEnable.isSelected())
        {
            dataColumn.prefWidthProperty().bind(tableView.widthProperty().divide(cloumnCount));
        }
    }

    public void setTooltip()
    {
        ToolTipManager.sharedInstance().setInitialDelay(10);
        ToolTipManager.sharedInstance().setReshowDelay(10);
        ToolTipManager.sharedInstance().setDismissDelay(5000);

        Tooltip tpHome = new Tooltip("Home");
        tpHome.getStyleClass().add("toolTip");
        homeTab.setTooltip(tpHome);

        Tooltip tpConnect = new Tooltip("Connect");
        tpConnect.getStyleClass().add("toolTip");
        connectTab.setTooltip(tpConnect);

        Tooltip tpConfigureTab = new Tooltip("Configure");
        tpConfigureTab.getStyleClass().add("toolTip");
        configureTab.setTooltip(tpConfigureTab);

        Tooltip tpReadTab = new Tooltip("Read");
        tpReadTab.getStyleClass().add("toolTip");
        readTab.setTooltip(tpReadTab);

        Tooltip tpHelpTab = new Tooltip("Help");
        tpHelpTab.getStyleClass().add("toolTip");
        helpTab.setTooltip(tpHelpTab);

        Tooltip tpReloadDevices = new Tooltip("Reload Devices");
        tpReloadDevices.getStyleClass().add("toolTip");
        reloadDevicesButton.setTooltip(tpReloadDevices);

        Tooltip tpAntennaDetection = new Tooltip("Enable antenna detection");
        tpAntennaDetection.getStyleClass().add("toolTip");
        antennaDetection.setTooltip(tpAntennaDetection);

        Tooltip tptagResultColumn = new Tooltip("Select column to display on tag results.");
        tptagResultColumn.getStyleClass().add("toolTip");

        metaDataAntenna.setTooltip(tptagResultColumn);
        metaDataProtocol.setTooltip(tptagResultColumn);    
        metaDataFrequency.setTooltip(tptagResultColumn);   
        metaDataPhase.setTooltip(tptagResultColumn);
        
        Tooltip toolTip = new Tooltip("Click to save the configurations.");
        toolTip.getStyleClass().add("toolTip");
        applyButton.setTooltip(toolTip);
        
        toolTip = new Tooltip("Click to load the configuration file.");
        toolTip.getStyleClass().add("toolTip");
        loadConfigButton.setTooltip(toolTip);
        
        toolTip = new Tooltip("Click to save the configurations to file.");
        toolTip.getStyleClass().add("toolTip");
        saveConfigButton.setTooltip(toolTip);
        
        toolTip = new Tooltip("Click to choose the firmware.");
        toolTip.getStyleClass().add("toolTip");
        loadFirmware.setTooltip(toolTip);
        
        toolTip = new Tooltip("Click to update the firmware.");
        toolTip.getStyleClass().add("toolTip");
        updateFirmware.setTooltip(toolTip);
        
        toolTip = new Tooltip("Click to revert to module default settings.");
        toolTip.getStyleClass().add("toolTip");
        revertButton.setTooltip(toolTip);
    }

    //Read Data From Reader
    public void read()
    {
        // Create and add tag listener
        readListener = new PrintListener();
        exceptionListener = new TagReadExceptionReceiver();
        statsListener = new ReaderStatsListener();
        r.addStatsListener(statsListener);
        r.addReadListener(readListener);
        r.addReadExceptionListener(exceptionListener);
    }

    class PrintListener implements ReadListener
    {
        @Override
        public void tagRead(Reader r, TagReadData tr)
        {
            try
            {
                String tagEpc = tr.tag.epcString();
                String key = tagEpc;
                int count = tr.getReadCount();
                if(embeddedReadUnique.isSelected())
                {
                    if(tr.data.length > 0)
                    {
                       key += ""+ReaderUtil.byteArrayToHexString(tr.data);
                    }
                    else
                    {
                        return;
                    }
                }
                
                totalTag+=count;
                if(tagData.containsKey(key))
                {
                    TagReadData trd = (TagReadData)tagData.get(key);
                    trd.readBase = tr.readBase;
                    trd.rssi = tr.rssi;
                    trd.readCount += count;
                    trd.antenna = tr.antenna;
                    trd.readProtocol = tr.readProtocol;
                    trd.frequency = tr.frequency;
                    trd.phase = tr.phase;
                    trd.data = tr.data;
                }
                else
                {
                    uniqueTag++;
                    tagData.put(key, tr);
                }
            }
            catch (Exception e)
            {
            }
        }
    }
    
    class  TagReadExceptionReceiver implements ReadExceptionListener
    {
        @Override
        public void tagReadException(Reader r, ReaderException re)
        {
           if(false 
                   || re.getMessage() == null  
                   || re.getMessage().contains("Invalid argument")
                   || re.getMessage().contains("Timeout"))
           {
                   new Thread(disConnectThread).start();
                   showWarningErrorMessage("error", "Connection lost");
           }
           else if(!isConnected && re.getMessage().contains("Failed to connect with baudrate"))
           {
              showWarningErrorMessage("warning", re.getMessage());
           }
           else
           {

           }
        }
    }
   
    public Task createProgress()
    {
        return new Task()
        {
            @Override
            protected Object call() throws Exception
            {
                for (int i = 0; i < 10; i++)
                {
                    Thread.sleep(2000);
                    updateMessage("2000 milliseconds");
                    updateProgress(i + 1, 10);
                }
                return true;
            }
        };
    }

   public void disableModuleUnsupportedFeatures()
    {
        try
        {
            if (readerModel.contains("M5e") || readerModel.equalsIgnoreCase("M6e Nano"))
            {
                link640Khz.setDisable(true);
                link640Khz.setOpacity(disableOpacity);
                fm0.setDisable(true);
                fm0.setOpacity(disableOpacity);
                tari6_25us.setDisable(true);
                tari6_25us.setOpacity(disableOpacity);
                tari12_5us.setDisable(true);
                tari12_5us.setOpacity(disableOpacity);
            }
            else
            {
                link640Khz.setDisable(false);
                link640Khz.setOpacity(enableOpacity);
                fm0.setDisable(false);
                fm0.setOpacity(enableOpacity);
                tari6_25us.setDisable(false);
                tari6_25us.setOpacity(enableOpacity);
                tari12_5us.setDisable(false);
                tari12_5us.setOpacity(enableOpacity);
            }

            if(!isAutonomousSupported )
            {
                autonomousRead.disableProperty().setValue(true);
                autonomousRead.setOpacity(disableOpacity);
                gpiTriggerRead.disableProperty().setValue(true);
                gpiTriggerRead.setOpacity(disableOpacity);
                showWarningErrorMessage("warning", "Autonomous operation is not supported on current firmware");
            }
            else
            {
               autonomousRead.disableProperty().setValue(false);
               autonomousRead.setOpacity(enableOpacity);
               gpiTriggerRead.disableProperty().setValue(false);
               gpiTriggerRead.setOpacity(enableOpacity); 
            }
            
            if (readerModel.equalsIgnoreCase("M6e Micro") || readerModel.equalsIgnoreCase("M6e Micro USB")
                    || readerModel.equalsIgnoreCase(Constants.M6E_MICRO_USB_PRO))
            {
                autoReadGpi3.setVisible(false);
                autoReadGpi4.setVisible(false);
            }
            else
            {
                autoReadGpi3.setVisible(true);
                autoReadGpi4.setVisible(true);
            }

            ipx256.setVisible(false);
            ipx64.setVisible(false);
            iso18000.setVisible(false);

            TagProtocol[] protocols = (TagProtocol[]) r.paramGet("/reader/version/supportedProtocols");
            TagProtocol currentProto =(TagProtocol) r.paramGet("/reader/tagop/protocol");
            supportedProtocols = new ArrayList<String>();
            supportedProtocols.add("GEN2");
        }
        catch (ReaderException ex)
        {
            
        }
    }
   
   public void setHomeContent()
   {
       WebView browser = new WebView();
       WebEngine webEngine = browser.getEngine();
       URL url = getClass().getResource("/fxml/Home.html");
       webEngine.load(url.toExternalForm());
       homeBorderPane.setCenter(browser);
   }

   @FXML
   private void getStarted(MouseEvent event)
   {
       mainTabs.getSelectionModel().select(connectTab);
   }
   
   @FXML
   private void showAboutInfo(MouseEvent event)
   {
        showAboutInfo();
   }
   
   private void showAboutInfo()
   {
       WebView browser = new WebView();
       WebEngine webEngine = browser.getEngine();
       URL url = getClass().getResource("/fxml/About.html");
       webEngine.load(url.toExternalForm());
       helpBorderPane.setCenter(browser);
       helpBorderPane.setVisible(true);
   }
   
   @FXML
   private void showSupportedDevicesInfo(MouseEvent event)
   {
       helpBorderPane.getChildren().clear();
       mainTabs.getSelectionModel().select(helpTab);
       WebView browser = new WebView();
       WebEngine webEngine = browser.getEngine();
       URL url = getClass().getResource("/fxml/SupportedDevices.html");
       webEngine.load(url.toExternalForm());
       helpBorderPane.setCenter(browser);
       helpBorderPane.setVisible(true);
   }
   
   @FXML
   private void showHelpInfo(MouseEvent event)
   {
       mainTabs.getSelectionModel().select(helpTab);
   }
   
   @FXML
   private void showConnectInfo(MouseEvent event)
   {
       mainTabs.getSelectionModel().select(helpTab);
       helpBorderPane.getChildren().clear();
       WebView browser = new WebView();
       WebEngine webEngine = browser.getEngine();
       URL url = getClass().getResource("/fxml/Connect.html");
       webEngine.load(url.toExternalForm());
       helpBorderPane.setCenter(browser);
       helpBorderPane.setVisible(true);
   }
   
    @FXML
    private void showConfigureInfo(MouseEvent event)
    {
        mainTabs.getSelectionModel().select(helpTab);
        helpBorderPane.getChildren().clear();
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        URL url = getClass().getResource("/fxml/Configure.html");
        webEngine.load(url.toExternalForm());
        helpBorderPane.setCenter(browser);
        helpBorderPane.setVisible(true);
    }
   
   @FXML
   private void showReadInfo(MouseEvent event)
   {
       mainTabs.getSelectionModel().select(helpTab);
       helpBorderPane.getChildren().clear();
       WebView browser = new WebView();
       WebEngine webEngine = browser.getEngine();
       URL url = getClass().getResource("/fxml/Read.html");
       webEngine.load(url.toExternalForm());
       helpBorderPane.setCenter(browser);
       helpBorderPane.setVisible(true);
   }

   @FXML
   private void showReadTab(MouseEvent event)
   {
       mainTabs.getSelectionModel().select(readTab);
   }

   @FXML
   private void revertToDefaultSettings(ActionEvent event)
   {
       try
       {
           int option = JOptionPane.showConfirmDialog(null, "Do you want to revert the module settings to factory defaults?", "Confirmation", JOptionPane.YES_NO_OPTION);
           if (option == JOptionPane.YES_OPTION)
           {
               clearConfiguationToolParameter();
               r.paramSet("/reader/userConfig", new SerialReader.UserConfigOp(SerialReader.SetUserProfileOption.CLEAR));
           }
           else
           {
               return;
           }
           initParams(r, changeListMap);
           showWarningErrorMessage("sucessfull", "Sucessfully reverted to default settings");
       }
       catch (Exception ex)
       {
           showWarningErrorMessage("error", "Failed to revert settings to default values due to "+ex.getMessage());
       }
   }

   private void clearConfiguationToolParameter() throws Exception
   {
       //r.paramSet("/reader/antenna/checkPort", false);
       antennaDetection.setSelected(false);
       configureAntennaBoxes(r);
       gpiTriggerRead.setSelected(false);
       embeddedReadEnable.setSelected(false);
       disableEmbeddedReadData();
       gpiTriggerRead.setSelected(false);
       changeUIOnGpiTriggerRead(true, disableOpacity);
       autonomousRead.setSelected(false);
       metaDataAntenna.setSelected(false);
       metaDataPhase.setSelected(false);
       metaDataProtocol.setSelected(false);
       metaDataFrequency.setSelected(false);
       selectedFilePath.clear();
       hopTime.clear();
       hopTable.clear();
   }

   @FXML
   private void showTroubleShootInfo(MouseEvent event)
   {
       mainTabs.getSelectionModel().select(helpTab);
       helpBorderPane.getChildren().clear();
       WebView browser = new WebView();
       WebEngine webEngine = browser.getEngine();
       URL url = getClass().getResource("/fxml/Troubleshoot.html");
       webEngine.load(url.toExternalForm());
       helpBorderPane.setCenter(browser);
       helpBorderPane.setVisible(true);
   }
   
   @FXML
   private void clearTags(ActionEvent event)
   {
        tagData.clear();
        tableView.getItems().clear();
        uniqueTag = 0;
        totalTag = 0;
        uniqueTagCount.setText(""+0);
        totalTagCount.setText(""+0); 
        temperature.setText("");
        statsTemperature = 0;
   }
   
   class ReaderStatsListener implements StatsListener
    {
      public void statsRead(SerialReader.ReaderStats readerStats)
      {
         if(statsTemperature != readerStats.temperature)
         {
            statsTemperature = readerStats.temperature;
         }
      }
    }

   private void isSupportsAutonomus(String versionsoftware)
   {       
       try
       {
           String[] version = versionsoftware.split("-")[0].split("\\.");
           int part1 = Integer.parseInt(version[0], 16);
           int part2 = Integer.parseInt(version[1], 16);
           int part3 = Integer.parseInt(version[2], 16);
           int part4 = Integer.parseInt(version[3], 16);
           SerialReader.VersionNumber vNumber = new SerialReader.VersionNumber(part1, part2, part3, part4);
           if(false 
               || ((readerModel.equalsIgnoreCase(Constants.M6E) || readerModel.equalsIgnoreCase(Constants.M6E_I_PRC) && vNumber.compareTo(new SerialReader.VersionNumber(1, 23, 0, 1)) >= 0)
               || (readerModel.equalsIgnoreCase(Constants.M6E_I_JIC)) && vNumber.compareTo(new SerialReader.VersionNumber(1, 21, 0, 5)) >= 0)
               || ((readerModel.equalsIgnoreCase(Constants.M6E_MICRO) || readerModel.equalsIgnoreCase(Constants.M6E_MICRO_USB))&& vNumber.compareTo(new SerialReader.VersionNumber(1, 5, 0, 1)) >= 0)
               ||  readerModel.equalsIgnoreCase(Constants.M6E_NANO)
               || (readerModel.equalsIgnoreCase(Constants.M6E_MICRO_USB_PRO) && vNumber.compareTo(new SerialReader.VersionNumber(1, 7, 2, 0)) >= 0))
           {
             isAutonomousSupported = true;
           }
       } 
       catch (Exception ex)
       {

       }     
   }

   private void setReaderStatus(int option)
   {
       switch(option)
       {
           case -1:
               connectStatus.fillProperty().set(Color.RED);
               statusLabel.setText("Not Connected");
               break;
           case 0:
               connectStatus.fillProperty().set(Color.ORANGE);
               statusLabel.setText("Connected");
               break;
           case 1:
               connectStatus.fillProperty().set(Color.GREEN);
               statusLabel.setText("Reading");
               break;
       }
   }
   
   class SaveTransportLogs implements TransportListener
   {
        @Override
        public void message(boolean tx, byte[] data, int timeout)
        {
            if(transportWriter != null)
            {
                try
                {
                    StringBuffer sb = new StringBuffer();
                    sb.append(tx ? "Sending: " : "Received: ");
                    for (int i = 0; i < data.length; i++)
                    {
                      if (i > 0 && (i & 15) == 0)
                        sb.append("\n");
                      sb.append(String.format(" %02x", data[i]));
                    }
                    transportWriter.append(sb);
                    transportWriter.newLine();
                    transportWriter.flush();
                }
                catch(IOException e)
                {

                }
            }
        }
    }
   
    private void createTransportLogsIntoFile()
   {
        try
        {  
            fileFilters = new ArrayList<String>();
            fileFilters.add("*.txt");
            chooseFile("Choose configuration file", "Configuration file (*.txt)", fileFilters);
            fileChooser.setInitialFileName("ACT_TransportLogs_"+dfhms.format(new Date(Calendar.getInstance().getTimeInMillis()))+".txt");
            File transLogFile = fileChooser.showSaveDialog(stage);
            if(transLogFile != null)
            {
                transLogFile.setWritable(true);
                transLogFile.setExecutable(true);
                FileWriter fileWriter = new FileWriter(transLogFile.getAbsoluteFile(),true);
                transportWriter = new BufferedWriter(fileWriter);
            }
            else
            {   
                isTransportLogsEnabled = false;
                cbTransportLogging.setSelected(false);
            }
        }
        catch(Exception e)
        {

        }
   }

   private void checkReadWritePowerOnUSBProModule()
   {
       if(readerModel.equalsIgnoreCase("M6e Micro USBPro") && !(rfRead.getText().equals("") || rfWrite.getText().equals("")))
       {
           float readPower = Float.parseFloat(rfRead.getText());
           float writePower = Float.parseFloat(rfWrite.getText());

           if(readPower >20 || writePower >20)
           {
               showWarningErrorMessage("warning", "Please make sure to provide additional DC power source to the reader");
           }
       }
   }

    private void disableEmbeddedReadData()
    {
        embeddedReadUnique.setDisable(true);
        embeddedReadUnique.setOpacity(disableOpacity);
        embeddedReadUnique.setSelected(false);
        embeddedMemoryBank.setDisable(true);
        embeddedMemoryBank.setOpacity(disableOpacity);
        embeddedStart.setDisable(true);
        embeddedStart.setOpacity(disableOpacity);
        embeddedEnd.setDisable(true);
        embeddedEnd.setOpacity(disableOpacity);
    }
    
    private void changeUIOnGpiTriggerRead(boolean status, double opacity)
    {
        autoReadGpi1.setDisable(status);
        autoReadGpi1.setOpacity(opacity);
        autoReadGpi2.setDisable(status);
        autoReadGpi2.setOpacity(opacity);
        autoReadGpi3.setDisable(status);
        autoReadGpi3.setOpacity(opacity);
        autoReadGpi4.setDisable(status);
        autoReadGpi4.setOpacity(opacity);
        autonomousRead.setSelected(status);
    }        

    private void getReaderDiagnostics() throws Exception
    {
        readerModel = (String) r.paramGet("/reader/version/model");
        firmwareVerson = (String) r.paramGet("/reader/version/software");
        hardWareVersion = (String) r.paramGet(TMConstants.TMR_PARAM_VERSION_HARDWARE);
        lRfidEngine.setText(readerModel);
        lFirmwareVersion.setText(firmwareVerson);
        lHardwareVersion.setText(hardWareVersion);
        lActVersion.setText(Constants.ACT_VERSION);
        lMercuryApiVersion.setText(Constants.MERCURY_API_VERSION);
    }

    class LoadSaveExceptionReciver implements ReadExceptionListener
    {
        @Override
        public void tagReadException(com.thingmagic.Reader r, ReaderException re)
        {
           if(!re.getMessage().toLowerCase().contains(Constants.SKIPPING))
           {
              loadSaveError.append(re.getMessage());
              loadSaveError.append(System.getProperty("line.separator"));
           }
        }
    }

    private void regionBasedPowerListener() throws ReaderException
    {
        try{
            int readPower = (Integer) r.paramGet("/reader/radio/readPower");
            int writePower = (Integer) r.paramGet("/reader/radio/writePower");
            if(readerModel.equalsIgnoreCase(Constants.M6E_MICRO_USB_PRO))
            {
                rfRead.setText("20");
                rfWrite.setText("20");
                readPower = 2000;
                writePower = 2000;
                r.paramSet("/reader/radio/readPower", readPower);
                r.paramSet("/reader/radio/writePower", writePower);
            }
            else
            {
                Float readPowerValue = (float)readPower / 100;
                Float writePowerValue = (float)writePower / 100;
                rfRead.setText(String.valueOf(readPowerValue));
                rfWrite.setText(String.valueOf(writePowerValue));
            }

            minReaderPower = (Integer) r.paramGet("/reader/radio/powerMin");
            minPower.setText(String.valueOf(minReaderPower / 100) + "dBm");
            minPower1.setText(String.valueOf(minReaderPower / 100) + "dBm");

            maxReaderPower = (Integer) r.paramGet("/reader/radio/powerMax");
            maxPower.setText(String.valueOf(maxReaderPower / 100.0) + "dBm");
            maxPower1.setText(String.valueOf(maxReaderPower / 100.0) + "dBm");
            checkReadWritePowerOnUSBProModule();

            changeListMap.put("readPower", rfRead.getText());
            changeListMap.put("writePower", rfWrite.getText());
            readPowerSlider.setMin(minReaderPower/100.0);
            writePowerSlider.setMin(minReaderPower/100.0);
            readPowerSlider.setMax(maxReaderPower / 100.0);
            writePowerSlider.setMax(maxReaderPower / 100.0);

            readPowerSlider.setValue(readPower / 100.0);
            writePowerSlider.setValue(writePower / 100.0);
        }
        catch(Exception e)
        {

        }
    }
}
