package controllers

import play.api._
import play.api.mvc._

import models.Movie
import models.Genre
import models.User
import views._

object Application extends Controller with Secured{


	/*
	Fronpage renderer, "index = action" opens frontpage, with movies list
	*/

	def index = Action { implicit request =>
		var movies = Movie.findAll
		var genres = Genre.getGenresWithMovies

		//@(movies: List[Movie])(genres: List[Genre])(navbar: Html)
		Ok(html.movies.list(Auth.username(request).getOrElse(null))("All")(movies)(genres))
  
	}
}