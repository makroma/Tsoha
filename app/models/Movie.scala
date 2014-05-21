package models

import play.api.db._
import play.api.Play.current
 
import anorm._
import anorm.SqlParser._

case class Movie(title: String, link:String, coverImg:String)	

object Movie{

   val simple = {
  		get[String]("movietitle") ~
	    get[String]("link") ~
	    get[String]("coverimg") map {
	      case title~link~coverImg => Movie(title, link, coverImg)
    	}
  	}

	def findAllSQL(): List[Movie] = {
	    DB.withConnection { implicit connection =>
	      SQL("select * from movies;").as(simple *)
	    }
	}
   
   	def findAll = findAllSQL.toList.sortBy(_.title)

   	def addMovie(title:String, link:String, coverimg:String) = {
   		DB.withConnection { implicit connection =>
			SQL(
				"""
				Insert into movies(movietitle, addingdate, 
				link, coverimg) values({t}, current_date, {link}, 
				{cover});
				""").on('t ->title, 'link ->link, 'cover -> coverimg).executeUpdate()	
		} 
   	}

}	