package controllers

import play.api.mvc.{Action, Controller}
import models.Movie
import models.Genre
import views._

object Movies extends Controller {

	/*
	* not on use yet!
	

	def list = Action { implicit request =>
	val movies = Movie.findAll
	var genres = Genre.allSorted
	Ok(views.html.movies.list(movies)(genres))
	}

  */
    /*
	Renders moviepage or return 404
	*/  

 	def showMovie(id: Int) = Action { implicit request =>
 		
    	Movie.findById(id).map { movie =>
	  		Ok(views.html.movies.movie(Auth.username(request).getOrElse(null))(movie))
		}.getOrElse(NotFound)
		
  	}

  	/*
	Renders filtered result
	*/  

 	def filterByGenre(genre: String) = Action { implicit request =>
	  	Ok(html.movies.list(Auth.username(request).getOrElse(null))(genre)(Movie.filterByGenre(genre))(Genre.getGenresWithMovies))
  	}
}