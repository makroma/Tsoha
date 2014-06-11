package models

import play.api.db._
import play.api.Play.current
 
import anorm._
import anorm.SqlParser._

case class Genre(id: anorm.Pk[Int] = NotAssigned, title: String)

object Genre{

  //Parse SQL with Anorm.parser

  val simple = {
    get[Pk[Int]]("genreid") ~
    get[String]("genrename") map {
      case id~title => Genre(id, title)
    }
  }

  def addGenre(title:String) = {
    println("addGenre: " + title)
    DB.withConnection { implicit connection =>
      SQL("Insert into genres (genrename) values({g});").on('g ->title).executeUpdate() 
    } 
  }

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

  def findGenreId(title:String): Option[Genre] = {
    DB.withConnection { implicit connection =>
        SQL("select genreid from genres where genrename = {t};").on('t -> title).as(simple.singleOpt)
    }
  }


  def getAll(): List[Genre] = {
    DB.withConnection { implicit connection =>
        SQL("select * from genres;").as(simple *)
    }
  }

  def getGenresWithMovies(): List[Genre] = {
    DB.withConnection { implicit connection =>
      SQL("""
        select DISTINCT g.genreid, g.genrename from genres g, genres_has_movies gm 
        where g.genreid = gm.genres_genreid and gm.genres_genreid is not null;
        """
      ).as(simple *)
    }
  }

  def allSorted = getAll.toList.sortWith(comp)

  def allSeq = getAll.toSeq.sortWith(comp)

  def genresWithMoviesSorted = getGenresWithMovies.toList.sortWith(comp)

  def comp(e1: Genre, e2: Genre) = (e1.title compareToIgnoreCase e2.title) < 0

}