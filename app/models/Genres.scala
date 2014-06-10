package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

import scala.collection.mutable.ArrayBuffer

case class Genres(genres:List[Genre])

object Genres{

  def addGenresToMovie(genres:List[String], movieId:anorm.Pk[Int]) = {

    val list = ArrayBuffer[Genre]()

    for(g<-genres) list += (Genre.findByGenre(g).getOrElse(null))
    println("add genre to movie: " )
    
    list.foreach( g =>
      DB.withConnection { implicit connection =>
        SQL("""
          Insert into genres_has_movies(genres_genreid, movies_movieid)
          values({gid}, {mid});
          """
        ).on('gid ->g.id, 'mid ->movieId).executeUpdate() 
      }
    )
  }

  /*
  * isAttachedToMovie is part of Genre.delete function
  * 'Cant delete genre if it is attached to the movie'
  */

  def isAttachedToMovie(genre: String):Boolean = {

    val count = {
      DB.withConnection { implicit connection =>
        SQL("""
          select count(*) as c from genres_has_movies
          where genres_genreid = (
            select genreid from genres where genrename = {g}
            );
          """
        ).on('g -> genre).apply().head
      }
    }
    if(count[Long]("c") < 1) true else false
  }

  /*
  * getMovieGenres returns list of attached genres
  */

  def getMovieGenres(movie:String):List[String] = {
    DB.withConnection { implicit connection =>
      SQL("""
        select genrename from genres g, genres_has_movies gm, movies m
        where g.genreid = gm.genres_genreid and gm.movies_movieid = m.movieid and 
        m.movietitle = {m}
        ;
        """
      ).on('m -> movie).as(str("genrename") *)
    }
  }
}