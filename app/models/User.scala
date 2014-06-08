package models

import play.api.db._
import play.api.Play.current
 
import anorm._
import anorm.SqlParser._

case class User(userid: anorm.Pk[Int] = NotAssigned, username: String, userpassword:String, admin:Boolean)


object User { //Data Access object - Companion


  //Parse SQL with Anorm.parser
  val simple = {
    get[Pk[Int]]("userid") ~
    get[String]("username") ~
    get[Boolean]("admin") map {
      case id~name~admin => User(id, name, null, admin)
    }
  }
  val full = {
    get[Pk[Int]]("userid") ~
    get[String]("username") ~
    get[String]("userpassword") ~
    get[Boolean]("admin") map {
      case id~name~userpassword~admin => User(id, name, userpassword, admin)
    }
  }

  def findAllSQL(): List[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from users;").as(User.simple *)
    }
  }

  def findById(id:Int): Option[User] = {
    println("findById..")
    DB.withConnection { implicit connection =>
      SQL("select * from users where userid = {id};").on('id -> id).as(User.full.singleOpt)
    }
  }


  def findByName(name:String): Option[User] = {
    DB.withConnection { implicit connection =>
        SQL("select * from users where username = {username};").on('username -> name).as(User.full.singleOpt)
    }
  }

  def idByName(name:String): Int = {
    print("idByName..")
    val user:Pk[Int] = findByName(name).map { user => user.userid }.get
    return user.get
  }

  def findAll = findAllSQL.toList.sortWith(comp)

  def comp(e1: User, e2: User) = (e1.username compareToIgnoreCase e2.username) < 0


  def checkUserPassword(username: String, password: String):Boolean = {
    print("checkUserPassword: ")
    var user:User = findByName(username).getOrElse(null)
    if(user!=null && user.userpassword == password && user.username == username) {
      println("userpassword mach")  
      true
    } 
    else {
      println("not maching")
      false
    } 
  }

  def addUser(username: String, password: String):Boolean ={
    println("model.User.addUser(" + username + ", " + password +")")
    DB.withConnection { implicit connection =>
      SQL("Insert into users(username, userpassword, admin) values({user}, {pssw}, false);").on('user ->username, 'pssw ->password).executeUpdate() 
    } 
    true  
  }

  def delete(name: String) {
    DB.withConnection { implicit c =>
      SQL("delete from users where username = {n}").on(
          'n -> name).executeUpdate()
    }
  }

  def updateNamePass(user:User, password:String) {
    DB.withConnection{ implicit connection =>
      SQL(
        """
        update users 
        set userpassword = {p}
        where userid = {id};
        """
        ).on('p -> password, 'id -> user.userid).executeUpdate()
    }
  }
}