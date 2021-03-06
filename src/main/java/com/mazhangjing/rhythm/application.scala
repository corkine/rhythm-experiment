package com.mazhangjing.rhythm

import java.nio.file.Paths
import java.util

import com.mazhangjing.lab._
import com.mazhangjing.lab.sound.{Capture, SimpleAudioFunctionMakerToneUtilsImpl, SimpleLongToneUtilsImpl, SimpleShortToneUtilsImpl}
import com.mazhangjing.rhythm
import com.mazhangjing.rhythm.MzjExperiment._
import com.mazhangjing.rhythm.help.{Helper, Utils}
import javafx.application.{Application, Platform}
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.event.Event
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.{GridPane, HBox, VBox}
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.util.Random

class VoiceDetectEventMaker(exp: Experiment, scene: Scene) extends EventMaker(exp, scene) {

  val logger: Logger = LoggerFactory.getLogger(classOf[VoiceDetectEventMaker])

  override def run(): Unit = {
    logger.debug("VoiceDetectEventMaker Called now....")
    val capture = new Capture()
    import com.mazhangjing.rhythm.help.Utils._
    capture.messageProperty().addListener {
      val value = capture.getMessage.toDouble
      if (value != 0.0) {
        logger.debug("Receive Sound Event Now..., Current Sound Signal is " + value)
        if (exp.getScreen != null) exp.getScreen.eventHandler(new Event(Event.ANY), exp, scene)
      }
    }
    new Thread(capture).start()
  }
}

class MzjApplication extends Application {

  class ExperimentModifier {
    val conditionChooseSet: collection.mutable.Set[Int]= new mutable.HashSet[Int]()
    def setEstimate(): Unit = {
      JUST_ESTIMATE = true
      JUST_COUNTING = false
    }
    def setCounting(): Unit = {
      JUST_ESTIMATE = false
      JUST_COUNTING = true
    }
    def setAll(): Unit = {
      JUST_ESTIMATE = false
      JUST_COUNTING = false
    }
    def setTest(): Unit = {
      IS_DEBUG = true
      IS_FAST_DEBUG = false
    }
    def setFastTest(): Unit = {
      IS_DEBUG = true
      IS_FAST_DEBUG = true
    }
    def setReal(): Unit = {
      IS_DEBUG = false
      IS_FAST_DEBUG = false
    }
    def setConditionChooseSet(selected: Boolean, conditionNumber: Int): Unit = {
      if (selected) conditionChooseSet.add(conditionNumber)
      else conditionChooseSet.remove(conditionNumber)
    }
    def setSoundImplMode(n: String): Unit = {
      n.toUpperCase match {
        case i if i.contains("SHORT") => SOUND_IMPL_CLASS = classOf[SimpleShortToneUtilsImpl]
        case i if i.contains("LONG") => SOUND_IMPL_CLASS = classOf[SimpleLongToneUtilsImpl]
        case i if i.contains("AUDIO") => SOUND_IMPL_CLASS = classOf[SimpleAudioFunctionMakerToneUtilsImpl]
      }
    }
    def setHz(hz: Double): Unit = {
      SUBJECT_HZ = hz
    }
    def setExperimentData(experimentData: ExperimentData): Unit = {
      MzjExperiment.initExperiment(experimentData, conditionChooseSet.toSet)
    }
    def setTrialRepeat(number: Int): Unit = {
      TRIAL_COUNT = number
    }
    def getTrialRepeat: String = {
      TRIAL_COUNT.toString
    }
  }

  class ExperimentController {
    import com.mazhangjing.rhythm.help.Utils._

    def initControlWithExp(gridPane: GridPane, rowIndex: Int): Unit = {
      val ecGroup = new ToggleGroup
      val estimateBtn = new ToggleButton("??????")
      val countingBtn = new ToggleButton("??????")
      val allBtn = new ToggleButton("????????????")
      ecGroup.getToggles.addAll(estimateBtn, countingBtn, allBtn)
      gridPane.add(new Label("??????"),0,rowIndex)
      gridPane.add({
        val b1 = new HBox()
        b1.getChildren.addAll(estimateBtn, countingBtn, allBtn)
        b1.setSpacing(5); b1}, 1,rowIndex)

      ecGroup.selectedToggleProperty().addListener {
        if (ecGroup.getSelectedToggle == estimateBtn) {
          modifier.setEstimate()
        } else if (ecGroup.getSelectedToggle == countingBtn) {
          modifier.setCounting()
        } else if (ecGroup.getSelectedToggle == allBtn) {
          modifier.setAll()
        }
      }

      ecGroup.selectToggle(allBtn)
    }

    def initControlWithDebug(gridPane: GridPane, rowIndex: Int): Unit = {
      val trialGroup = new ToggleGroup
      val testBtn = new ToggleButton("??????")
      val fastTestBtn = new ToggleButton("????????????")
      val realBtn = new ToggleButton("????????????")
      trialGroup.getToggles.addAll(testBtn, fastTestBtn, realBtn)
      gridPane.add(new Label("??????"),0,rowIndex)
      gridPane.add({
        val t = new HBox()
        t.getChildren.addAll(testBtn, fastTestBtn, realBtn)
        t.setSpacing(5); t}, 1,rowIndex)

      trialGroup.selectedToggleProperty().addListener {
        val selected = trialGroup.getSelectedToggle
        if (selected == testBtn) {
          modifier.setTest()
        } else if (selected == fastTestBtn) {
          modifier.setFastTest()
        } else if (selected == realBtn) {
          modifier.setReal()
        }
      }

      trialGroup.selectToggle(realBtn)
    }

    def initControlWithCondition(gridPane: GridPane, rowIndex: Int): Unit = {
      val c1 = new ToggleButton("????????????")
      val c2 = new ToggleButton("??????????????????")
      val c3 = new ToggleButton("??????????????????")
      val c4 = new ToggleButton("??????????????????")
      val c5 = new ToggleButton("???????????????")
      val c6 = new Button("??????")
      gridPane.add(new Label("??????"),0,rowIndex)
      gridPane.add({
        val t = new HBox()
        t.getChildren.addAll(c1,c2,c3,c4,c5,c6)
        t.setSpacing(5); t},1,rowIndex)

      c1.selectedProperty().addListener((_, _, n) => modifier.setConditionChooseSet(n,1))
      c2.selectedProperty().addListener((_, _, n) => modifier.setConditionChooseSet(n,2))
      c3.selectedProperty().addListener((_, _, n) => modifier.setConditionChooseSet(n,3))
      c4.selectedProperty().addListener((_, _, n) => modifier.setConditionChooseSet(n,4))
      c5.selectedProperty().addListener((_, _, n) => modifier.setConditionChooseSet(n,5))
      c6.setOnAction(_ => {
        if (c1.isSelected && c2.isSelected && c3.isSelected
          && c4.isSelected && c5.isSelected) {
          c1.setSelected(false)
          c2.setSelected(false)
          c3.setSelected(false)
          c4.setSelected(false)
          c5.setSelected(false)
        } else {
          c1.setSelected(true)
          c2.setSelected(true)
          c3.setSelected(true)
          c4.setSelected(true)
          c5.setSelected(true)
        }
      })
    }

    def initControlWithSound(gridPane: GridPane, rowIndex: Int): Unit = {
      val soundChoose = {
        val t = new ChoiceBox[String]()
        t.getItems.add("SimpleShortToneUtilsImpl")
        t.getItems.add("SimpleLongToneUtilsImpl")
        t.getItems.add("SimpleAudioFunctionMakerToneUtilsImpl"); t}
      gridPane.add(new Label("??????"), 0, rowIndex)
      gridPane.add(soundChoose, 1,rowIndex)

      soundChoose.getSelectionModel.selectedItemProperty().addListener((_, _, n) => {
        modifier.setSoundImplMode(n)
      })

      soundChoose.getSelectionModel.selectLast()
    }

    def initControlWithTrialRepeat(gridPane: GridPane, rowIndex: Int): Unit = {
      val label = new Label("??????")
      label.setAccessibleHelp("?????? Trial ????????????")
      val text = new TextField()
      text.setPromptText(modifier.getTrialRepeat)
      text.textProperty().addListener((_,_,n) => {
        if (!n.isEmpty) {
          val value = n.toInt
          modifier.setTrialRepeat(value)
        }
      })
      gridPane.add(label, 0, rowIndex)
      gridPane.add(text, 1,rowIndex)
      GridPane.setFillWidth(text,false)
    }

    def getControlPane(gridPane: GridPane): GridPane = {
      val condition1 = new Label("????????????")
      gridPane.add(condition1, 0,0)

      initControlWithExp(gridPane, 1)
      initControlWithDebug(gridPane, 2)
      initControlWithCondition(gridPane, 3)
      initControlWithTrialRepeat(gridPane, 4)
      initControlWithSound(gridPane, 5)
      //gridPane.setStyle("-fx-border-color:red")
      //gridPane.setGridLinesVisible(true)
      gridPane
    }
  }

  class DataController {
    val name = new TextField()
    val gender = new ChoiceBox[String]()
    val id = new TextField()
    val hz = new TextField()
    val info = new TextField()

    val detailString: SimpleStringProperty =
      new SimpleStringProperty("?????????????????????????????????????????????????????????????????? ??????.obj ??????????????????????????????")

    var experimentData:ExperimentData = _

    def getInformationPane(gridPane: GridPane): GridPane =  {
      name.setPromptText("??????")
      name.setText("NoName")
      gender.setItems({
        val a = FXCollections.observableArrayList[String]()
        a.addAll("???","???")
        a
      })
      gender.getSelectionModel.selectFirst()
      id.setPromptText("??????")
      id.setText((Random.nextInt(10000) + 23333).toString)
      hz.setText("1.5")
      hz.setPromptText("????????????")
      info.setPromptText("??????")

      gridPane.add(new Label("????????????"), 0,0)
      gridPane.add(new Label("??????"), 0,1)
      gridPane.add(new Label("??????"),0,2)
      gridPane.add(new Label("??????"),0,3)
      gridPane.add(new Label("??????"),0,5)
      gridPane.add(new Label("??????"),0,6)
      gridPane.add(name,1,1)
      gridPane.add(gender,1,2)
      val detail = new Text("")
      detail.textProperty().bind(detailString)
      detail.setFill(Color.DARKGRAY)
      val load = new Button("??? ??????.obj ?????????")
      val hbox = new HBox(); hbox.setSpacing(5)
      hbox.getChildren.addAll(id, load)
      gridPane.add(hbox,1,3)
      gridPane.add(detail, 1,4)
      gridPane.add(hz,1,5)
      gridPane.add(info,1,6)

      load.textProperty().bind(
        new SimpleStringProperty("??? ").concat(id.textProperty()).concat(".obj ??????"))
      load.setOnAction(_ => {
        Platform.runLater(() => {
          doLoadExistData()
        })
      })

      val shuffleBtn = new Hyperlink("????????????")
      val timeChecker = new Hyperlink("????????????")

      shuffleBtn.setOnAction(_ => {
        val alert = new Alert(AlertType.INFORMATION)
        alert.setHeaderText("Lucky Order")
        val ints = Random.shuffle(List.range(1,6))
        alert.setContentText(ints.mkString(" - "))
        alert.showAndWait()
      })
      timeChecker.setOnAction(_ => {
        val timer = new rhythm.help.Helper.Timer(null)
        timer.stage.show()
      })

      val cmd = new HBox(); cmd.setSpacing(13)
      cmd.getChildren.addAll(start, isFullScreen, shuffleBtn, timeChecker)
      cmd.setAlignment(Pos.CENTER_LEFT)
      GridPane.setConstraints(cmd,0,7,2,1)
      GridPane.setMargin(cmd,new Insets(30,0,0,0))
      gridPane.getChildren.add(cmd)

      //gridPane.setStyle("-fx-border-color: pink")
      //gridPane.setGridLinesVisible(true)
      gridPane
    }

    private[this] def getDetailInfo(experimentData: ExperimentData): String = {
      if (experimentData.trialData != null &&
        !experimentData.trialData.isEmpty)
        Utils.getStatus(experimentData, withStatus = false)
      else ""
    }

    private[this] def doLoadExistData(): Unit = {
      try {
        val data = ExperimentData.loadWithObject(Paths.get(id.getText() + ".obj"))
        this.experimentData = data
        name.setText(this.experimentData.userName)
        id.setText(this.experimentData.userId.toString)
        hz.setText(this.experimentData.prefHz.toString)
        gender.getSelectionModel.select(this.experimentData.gender)
        info.setText(this.experimentData.information)
        getDetailInfo(experimentData) match {
          case i if !i.isEmpty => detailString.set(i)
          case _ =>
        }
      } catch {
        case e: Throwable =>
          this.experimentData = null
          val alert = new Alert(AlertType.ERROR)
          alert.setHeaderText("??????????????????")
          alert.setContentText("?????????????????????????????????????????????????????????????????????????????????????????????")
          e.printStackTrace(System.err)
          alert.showAndWait()
      }
    }

    def initDataBeforeRunExperiment(): Unit = {
      if (experimentData == null) {
        //????????????
        experimentData = new ExperimentData()
        experimentData.userName = name.getText().trim
        experimentData.gender = gender.getSelectionModel.getSelectedIndex
        experimentData.userId = id.getText().toInt
        experimentData.information = info.getText().trim
        experimentData.prefHz = hz.getText().trim.toDouble
        modifier.setHz(experimentData.prefHz)
      } else {
        //????????????
        //???????????????????????????????????? Information??????????????????????????????????????????ID ??? prefHZ
        experimentData.information = info.getText().trim
        modifier.setHz(experimentData.prefHz)
      }
      modifier.setExperimentData(experimentData)
    }
  }

  val isFullScreen: CheckBox = {
    val t = new CheckBox("????????????")
    t.setSelected(true); t
  }
  val start = new Button("RUN EXPERIMENT")

  val modifier = new ExperimentModifier

  val controlController = new ExperimentController

  val dataController = new DataController

  def getRoot: Parent = {
    def initPane: GridPane = {
      val gridPane = new GridPane()
      gridPane.setAlignment(Pos.CENTER_LEFT)
      gridPane.setHgap(5)
      gridPane.setVgap(10)
      gridPane
    }
    val pane1 = dataController.getInformationPane(initPane)
    val pane2 = controlController.getControlPane(initPane)
    val vbox = new VBox()
    vbox.setSpacing(20)
    vbox.setAlignment(Pos.CENTER)
    vbox.getChildren.addAll(pane2,pane1)
    vbox.setPadding(new Insets(0,0,0,60))
    vbox
  }

  val configureScene = new Scene(getRoot, 700, 663)

  override def start(stage: Stage): Unit = {
    stage.setTitle(s"Rhythm Experiment Configure - ${Log.version}")
    stage.setScene(configureScene)
    start.setOnAction(_ => {
      dataController.initDataBeforeRunExperiment() //?????? experimentData ????????????

      val runner: ExpRunner = new ExpRunner {
        override def initExpRunner(): Unit = {
          val makers = new util.HashSet[String]()
          makers.add("com.mazhangjing.rhythm.VoiceDetectEventMaker")
          setEventMakerSet(makers)
          val set = new util.HashSet[OpenedEvent]()
          set.add(OpenedEvent.KEY_PRESSED)
          setOpenedEventSet(set)
          setExperimentClassName("com.mazhangjing.rhythm.MzjExperiment")
          setVersion("0.0.1")
          setFullScreen(isFullScreen.isSelected)
        }
      }
      val experimentStage = new Stage()
      val helper = new SimpleExperimentHelperImpl(runner)
      helper.initStage(experimentStage)
      experimentStage.setTitle("Rhythm Experiment Runner")
      experimentStage.show()
    })

    stage.show()
  }
}

object Log {
  val log: String = """
      |1.0.0 2019-04-26 ????????? Sound ?????????  AudioFunction???AudioMaker ?????????
      |      ????????? SimpleAudioFUnctionMakerTOneUtilsImpl ?????????
      |1.0.1 2019-04-27 ??????????????????????????????????????????????????????????????? Windows ?????????????????????????????????
      |1.0.2 2019-04-27 ????????? csv ??????????????????
      |1.0.3 2019-04-28 ????????? Recorder ?????????????????? MzjApplication ??????
      |1.0.4 2019-04-29 ????????? Processor ???????????????????????? Recorder ??????????????????????????? GUI ?????????????????????
      |      ??????????????? MzjApplication ??????????????????????????????????????? Pane ???????????????????????? Controller ??????
      |1.0.5 2019-04-29 ????????? ExperimentData object ?????? UUID ??????????????????????????????????????????
      |      ????????? MzjApplication ?????? SOUND_IMPL_CLASS ????????? MzjApplication???MzjExperiment???Trial ???????????????
      |      ????????? Trial ?????? MzjExp ??????????????? MzjApp ?????? MzjExp ?????? className?????????????????????
      |1.0.6 2019-04-30 ?????????????????? Trial ?????????????????????????????????????????????????????????
      |1.0.7 2019-04-30 ????????????????????????????????????????????????????????????????????????
      |1.0.8 2019-05-01 ?????????????????????????????????????????????????????????????????????????????????????????????????????? DataProcess object ??????
      |      ???????????????????????? early???late ????????????????????????????????? Helper.Process???SequenceImpl???Factory ?????????????????? API???
      |1.0.9 2019-05-01 ??????????????????????????????????????????????????? API???
    """.stripMargin

  def version: String = {
    val version = """(\d+\.\d+\.\d+)""".r
    val array = version.findAllIn(log).toArray
    array.reverse.headOption match {
      case None => "NaN"
      case Some(v) => v
    }
  }
}
