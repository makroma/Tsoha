package models

import play.api.db._
import play.api.Play.current
 
import anorm._
import anorm.SqlParser._
import java.util.{Date}


case class Movie(var id: anorm.Pk[Int] = NotAssigned, title: String, 
          link:Option[String], coverImg:Option[String], var genres:List[String], 
          plot:Option[String], year:Option[Int])

case class MovieUpdate(link:Option[String], coverImg:Option[String], 
          var genres:List[String], plot:Option[String], year:Option[Int])
        

object Movie{

  /*
  Select * from genres_has_movies where movieid ={movieid};
  */      

  val simple = {
      get[Pk[Int]]("movieid") ~
      get[String]("movietitle") ~
      get[Option[String]]("link") ~
      get[Option[String]]("coverimg") ~ 
      get[Option[String]]("plot") ~
      get[Option[Int]]("year")map {
        case id~title~link~coverImg~plot~year => Movie(id, title, link, coverImg, null, plot, year)
    }
  }

  def findAllSQL(): List[Movie] = {
      DB.withConnection { implicit connection =>
        SQL("select * from movies;").as(simple *)
      }
  }

  def findByName(title:String): List[Movie] = {
    DB.withConnection { implicit connection =>
      SQL("select * from movies where movietitle = {t};").on('t -> title).as(simple *)
    }
  }

  /**
  * Fuction is used to show movie page
  */

  def findById(id:Int): Option[Movie] = {
    DB.withConnection { implicit connection =>
      SQL("select * from movies where movieid = {id};").on('id -> id).as(simple.singleOpt)
    }
  }

  def getID(title:String):anorm.Pk[Int] = {
    DB.withConnection { implicit connection =>
      val movie = SQL("select * from movies where movietitle = {title};").on('title -> title).as(simple.singleOpt)
      val res:anorm.Pk[Int] = movie.map { m => m.id }.getOrElse(NotAssigned)
      return res
    }
  }
   
  def findAll = findAllSQL.toList.sortBy(_.title)

  def addMovie(movie:Movie) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
        Insert into movies(movietitle, year, plot, addingdate, link, coverimg) 
        values({t}, {y}, {p}, current_date, {link}, 
        {cover});
        """
      ).on('t ->movie.title, 'y -> movie.year, 'p -> movie.plot, 'link ->movie.link, 'cover -> movie.coverImg).executeUpdate()  
    } 
  }
  
  /*
  * filterByGenre return filtered list of movies
  */

  def filterByGenre(genre: String): List[Movie] = {
    DB.withConnection { implicit connection =>
      SQL("""
        select * from movies m, genres_has_movies gm, genres g
        where m.movieid = gm.movies_movieid and gm.genres_genreid = g.genreid and
        g.genrename = {g};
        """
      ).on('g -> genre).as(simple *)
    }
  }

  /*
  * Returns adding date of a movie
  */
  
  def addingDate(movie: String):Option[String] = {
    DB.withConnection { implicit connection =>
      SQL("""
        select to_char(addingdate,'FMMonth FMDDth YYYY') as date from movies where movietitle = {m} ;
        """
      ).on('m -> movie).as(str("date").singleOpt)
    }
  }

  /*
  * Delete movie function also deletes genre attachments from genres_has_movies
  */

  def delete(movie: String) = {
    /*Delete attached genre*/
    Genres.deleteMovieGenre(getID(movie).get)

    DB.withConnection { implicit c =>
      SQL("delete from movies where movietitle = {m}").on(
          'm -> movie
      ).executeUpdate()
    }
  }
  def update(movie:Movie) = {
     DB.withConnection { implicit connection =>
      SQL(
        """
        update movies
        set year = {y},
        plot = {p},
        link = {l},
        coverimg = {c}
        where movietitle = {t};
        """
      ).on('y -> movie.year, 'p -> movie.plot, 'l -> movie.link, 'c -> movie.coverImg, 't -> movie.title).executeUpdate()
    }
  }
} 