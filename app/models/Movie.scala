package models

import play.api.db._
import play.api.Play.current
 
import anorm._
import anorm.SqlParser._

case class Movie(title: String, link:String, coverImg:String, genres:List[Genre])	

object Movie{

	/*
	Select * from genres_has_movies where movieid ={movieid};
	*/

   val simple = {
  		get[String]("movietitle") ~
	    get[String]("link") ~
	    get[String]("coverimg") map {
	      case title~link~coverImg => Movie(title, link, coverImg, null)
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