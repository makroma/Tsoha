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
}