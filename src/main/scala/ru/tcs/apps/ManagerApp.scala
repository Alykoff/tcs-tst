package ru.tcs.apps

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{BorderLayout, FlowLayout}
import java.io.File
import java.lang.ProcessBuilder.Redirect
import javax.swing.{JFrame, _}

import akka.actor._
import ru.tcs.actors.MasterActor
import ru.tcs.utils.Messages._
import ru.tcs.utils.Utils._
import ru.tcs.utils.Constants._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Success, Try}

import java.nio.file._

object ManagerApp extends App {
  val config = configure(args, MANAGER_ROLE)
  val system = ActorSystem(ACTOR_SYSTEM_NAME, config)
  val manager = system.actorOf(Props[MasterActor], name = MANAGER_NAME)
  new ManagerWin(manager)

//  @deprecated
//  def newProcess(): Unit = {
//    val currentRelativePath = Paths.get("")
//    val absPath = currentRelativePath.toAbsolutePath.toString
//    val separator = File.separator
//    println("Current relative path is: " + absPath)
//    val pb = new ProcessBuilder("java", s"""-classpath=${absPath}${separator}""",  "ru.tcs.apps.WorkerApp")
//    val env = pb.environment()
//    pb.directory(new File("""/c:/tmp"""))
//    val log = new File(s"${System.currentTimeMillis}.log")
//    pb.redirectErrorStream(true)
//    pb.redirectOutput(Redirect.appendTo(log))
//    val p = pb.start()
//  }
}

class ManagerWin(val manager: ActorRef) extends JFrame("Manager") {
  implicit def toActionListener(f: ActionEvent => Unit) = new ActionListener {
    def actionPerformed(e: ActionEvent) { f(e) }
  }
  import javax.swing.JFrame._
  setDefaultLookAndFeelDecorated(true)
  setDefaultCloseOperation(EXIT_ON_CLOSE)
  val textField = new JTextField
  textField.setText("100")
  val removeTextField = new JTextField
  removeTextField.setText("akka.tcp://ClusterSystem@127.0.0.1:")
  val nodesText = new JTextArea(4, 30)
  val button = new JButton("Change messenging interval")
  val buttonRemove = new JButton("Remove node")
  val buttonAdd = new JButton("Add node")
  //  val buttonMaxValue = new JButton("Drop max val")
//  val buttonProcess = new JButton("New proccess")

  button.addActionListener{ e: ActionEvent =>
    val DEFAULT_INTERVAL = 100
    val interval = Try(textField.getText.toInt).map{
      case x => if (x <= 0) DEFAULT_INTERVAL else x
    }.getOrElse(DEFAULT_INTERVAL)
    textField.setText(interval.toString)
    manager ! MasterCommandMessages.ChangeNodesInterval(interval.milliseconds)
  }

  buttonRemove.addActionListener{ e: ActionEvent =>
    val address = removeTextField.getText
    val parsedAddress = Try(AddressFromURIString.parse(address))
    parsedAddress match {
      case Success(adr) => manager ! MasterCommandMessages.RemoveNode(adr)
      case _ => println("Exception in remove node!")
    }
  }

//  buttonMaxValue.addActionListener{ e: ActionEvent =>
//    manager ! DropMaxVal
//  }

  buttonAdd.addActionListener{ e: ActionEvent =>
    val address = removeTextField.getText
    val parsedAddress = Try(AddressFromURIString.parse(address))
    parsedAddress match {
      case Success(adr) => manager ! MasterCommandMessages.AddNode(adr)
      case _ => println("Exception in add node!")
    }
  }

//  buttonProcess.addActionListener{ e: ActionEvent =>
//    ManagerApp.newProcess()
//  }


  setLayout(new BorderLayout)
  val panel = new JPanel
  panel.setLayout(new FlowLayout)
  add(panel, BorderLayout.CENTER)
  panel.add(textField)
  panel.add(button)
  panel.add(removeTextField)
  panel.add(buttonRemove)
  panel.add(buttonAdd)
//  panel.add(buttonMaxValue)
//  panel.add(buttonProcess)

  pack()
  setSize(400, 300)
  setVisible(true)
}


