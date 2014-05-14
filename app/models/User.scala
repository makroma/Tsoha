package models

import play.api.db._
import play.api.Play.current
 
import anorm._
import anorm.SqlParser._

case class User(var id: Int, var name: String)
//Model class:

object User { //Data Access object - Companion


	//Finder functions

	//Parse SQL
	val simple = {
	    get[String]("name") ~
	    get[Int]("id") map {
	      case name~id => User(id, name)
    	}
  	}


	def findAllSQL(): List[User] = {
	    DB.withConnection { implicit connection =>
	      SQL("select * from users;").as(User.simple *)
	    }
	}



	def findAll = findAllSQL.toList.sortBy(_.name)
/*
	def findByName(name: String) = users.find(_.name == name)

	def getUser(name: String): User ={
		for(u<-users) { 
		  if (u.name == name) return u
		}
		null
	}

	def findById(id: Long):User = {
		for(u<-users) { 
		  if (u.id == id) return u
		}
		null
	}

	def checkUserPassword(name: String, password: String):Boolean = {
		for(u<-users) { 
		  if (u.name == name && u.password == password) 
		    return true 
		}
		false
	}
	*/
}