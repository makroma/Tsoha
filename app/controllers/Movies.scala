package controllers

import play.api.mvc.{Action, Controller}
import models.Movie

object Movies extends Controller {

  def list = Action { implicit request =>
    val movies = Movie.findAll
    Ok(views.html.movies.list(movies))
  }
}