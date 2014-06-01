package models

import play.api.db._
import play.api.Play.current
 
import anorm._
import anorm.SqlParser._

case class Genre(title: String)

object Genre{

	//Parse SQL with Anorm.parser

	val simple = {
	    get[String]("genrename") map {
	      case title => Genre(title)
    	}
  	}

	def addGenre(title:String) = {
		println("addGenre: " + title)
		DB.withConnection { implicit connection =>
			SQL("Insert into genres (genrename) values({g});").on('g ->title).executeUpdate()	
		} 
	}

	/*
	TODO: do we need to delete instance also from movies_has_genres!
	*/

	def delete(title: String) {
		DB.withConnection { implicit c =>
			SQL("delete from genres where genrename = {title}").on(
		      'title -> title
		    ).executeUpdate()
		}
	}

	def findByGenre(title:String): Option[Genre] = {
		println("findByGenre " + title )
		DB.withConnection { implicit connection =>
	    	SQL("select * from genres where genrename = {t};").on('t -> title).as(simple.singleOpt)
	    }
	}

	def getAll(): List[Genre] = {
		DB.withConnection { implicit connection =>
	    	SQL("select * from genres;").as(simple *)
	    }
	}

	def allSorted = getAll.toList.sortWith(comp)
	def allSeq = getAll.toSeq.sortWith(comp)

	def comp(e1: Genre, e2: Genre) = (e1.title compareToIgnoreCase e2.title) < 0

}