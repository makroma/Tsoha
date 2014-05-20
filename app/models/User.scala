package models

import play.api.db._
import play.api.Play.current
 
import anorm._
import anorm.SqlParser._

case class User(userid:Int, username: String, userpassword:String, admin:Boolean)


object User { //Data Access object - Companion


	//Parse SQL with Anorm.parser
	val simple = {
		get[Int]("userid") ~
	    get[String]("username") ~
	    get[Boolean]("admin") map {
	      case id~name~admin => User(id, name, null, admin)
    	}
  	}
  	val full = {
  		get[Int]("userid") ~
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
		println("findByName..")
		DB.withConnection { implicit connection =>
	    	SQL("select * from users where username = {username};").on('username -> name).as(User.full.singleOpt)
	    }
	}

	def idByName(name:String): Int = {
		print("idByName..")
		val user:Int = findByName(name).map { user => user.userid }.getOrElse(-1)
		println(user)	
		return user
	}


	def findAll = findAllSQL.toList.sortBy(_.username)


	def checkUserPassword(username: String, password: String):Boolean = {
		var user:User = findByName(username).getOrElse(null)
		println(user)
		if(user!=null && user.userpassword == password && user.username == username) true
		else false
	}

	def addUser(username: String, password: String):Boolean ={
		println("model.User.addUser(" + username + ", " + password +")")
		DB.withConnection { implicit connection =>
			SQL("Insert into users(username, userpassword, admin) values({user}, {pssw}, false);").on('user ->username, 'pssw ->password).executeUpdate()	
		} 
		true	
	}
	
}