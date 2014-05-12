package models

case class Movie(val movieId: Int, val movieTitle: String)	

object Movie{

   var  movies = Set (
   	Movie(1, "Chicago 69"),
   	Movie(2, "Love Story in campus"),
	Movie(3, "Brutal ninja combat 2")
   )
   
   def findAll = movies.toList.sortBy(_.movieTitle)
}