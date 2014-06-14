package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

case class Genres(genres:List[Genre])

object Genres{

  def addGenresToMovie(genres:List[String], movieId:anorm.Pk[Int]) = {

    val list = for(g<-genres) yield Genre.findByGenre(g).get

    list.foreach( g =>
      DB.withConnection { implicit connection =>
        SQL("""
          Insert into genres_has_movies(genres_genreid, movies_movieid)
          values({gid}, {mid}) ;
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
        SQL(
          """
          select count(*) as c from genres_has_movies
          where genres_genreid = (
            select genreid from genres where genrename = {g}
          );
          """
        ).on('g -> genre).apply().head
      }
    }
    count[Long]("c") < 1 //if count "c" < 1 return true
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

  def deleteMovieGenre(movieid: Int) = {
    DB.withConnection { implicit c =>
      SQL("delete from genres_has_movies where movies_movieid = {id}"
        ).on('id -> movieid).executeUpdate()
    }
  }
}