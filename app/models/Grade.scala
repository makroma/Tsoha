
package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

case class Grade(userid:Int, movieid:Int, stars:Int)

object Grade{

  def giveGrade(userid:Int, movieid:Int, stars:Int) = {
    DB.withConnection { implicit connection =>
        SQL(
          """
          Insert into grades(users_userid, movies_movieid, stars)
          values({u}, {m}, {s});
          """
        ).on('g -> userid, 'm -> movieid, 's -> stars).executeUpdate() 
    }
  }

  def movieGrade(movieid:Int):Double = {

    val avg = DB.withConnection { implicit connection =>
      SQL(
        """
        select ROUND(CAST(avg(stars) as numeric), 2) as average from grades 
        where movies_movieid = {m} and movies_movieid is not null;
        """
      ).on('m -> movieid).as(get[Option[java.math.BigDecimal]]("average").singleOpt).get
    }
    if(avg == None) 0.0
    else avg.get.doubleValue
  }
}

