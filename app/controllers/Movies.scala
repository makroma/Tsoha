package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Flash
import play.api.data._
import play.api.data.Form._
import play.api.data.Forms.{mapping, number, nonEmptyText, boolean, tuple, text, seq, list, optional, ignored}
import play.api.mvc.{Action, Controller}

import models.Movie
import models.Genre
import models.Grade
import views._

object Movies extends Controller {


  val searchForm = Form(
    "search" -> nonEmptyText
  )

  /*
  * Frontpage with movies list and filtering options
  * Username will be passed as contructor param if exists
  */

  def frontPage = Action { implicit request =>
    var movies = Movie.findAll
    var genres = Genre.genresWithMoviesSorted

    //@(movies: List[Movie])(genres: List[Genre])(navbar: Html)
    Ok(html.movies.list(Auth.username(request).getOrElse(null))("All")(movies)(genres)(searchForm))
  
  }

  /*
  Renders filtered result
  */  

  def filterByGenre(genre: String) = Action { implicit request =>
    Ok(html.movies.list(Auth.username(request).getOrElse(null))(genre)(Movie.filterByGenre(genre))(Genre.genresWithMoviesSorted)(searchForm))
  }

  /*
  Renders moviepage or return 404
  */  

  def showMovie(id: Int) = Action { implicit request =>
    
    Movie.findById(id).map { movie =>
      Ok(views.html.movies.movie(Auth.username(request).getOrElse(null), movie, Grade.movieGrade(id)))
    }.getOrElse(NotFound)
  }

  def postSearch = Action { implicit request =>
    var movies = Movie.findAll
    var genres = Genre.genresWithMoviesSorted

    searchForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.movies.list(Auth.username(request).getOrElse(null))("All")(movies)(genres)(searchForm)),
      search => Redirect(routes.Movies.searchMovie(search))
    )
  }


  def searchMovie(search: String) = Action { implicit request =>
    
    var movies = Movie.search(search)
    var genre = Genre.genresWithMoviesSorted

    Ok(html.movies.list(Auth.username(request).getOrElse(null))("search")(movies)(Genre.genresWithMoviesSorted)(searchForm))
  }
}