package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Flash
import play.api.data._
import play.api.data.Form._
import play.api.data.Forms.{mapping, number, nonEmptyText, boolean, tuple, text, seq}
import play.api.mvc.{Action, Controller}

import models.User
import models.Aspirant
import models.Genre
import models.Movie

object Admin extends Controller {

  /*
  Dashboard renderer @(content: Html)(right: Html)(implicit flash: Flash)
  */

	def dashboard = Action { implicit request =>
		val users = User.findAllSQL
		Ok(views.html.admin.dashboard(null)(views.html.admin.users(users)))
	}

  /*
	Renders userpage or return 404
  camelUser(selectors)(where)(is)
	*/  

  def showUser(id: Int) = Action { implicit request =>
   // User.findCamelUser("*")("userid")(id.toString).map 
      User.findById(id).map {user =>
	  		Ok(views.html.admin.user(user))
		}.getOrElse(NotFound)
  }

  /*
  Userform for adding new user
  Aspirant -type have only username and password
  */

  val userForm: Form[Aspirant] = Form(
    mapping(
      "username" -> nonEmptyText.verifying(
        "Not unique username", User.findByName(_).isEmpty),
      "password" -> tuple(
          "main" -> nonEmptyText,
          "confirm" -> nonEmptyText).verifying( 
            "Passwords don't match", password => password._1 == password._2 
          ).transform(
            { case (main, confirm) => main },
            ( main: String) => ("", "")
        )
    )(Aspirant.apply)(Aspirant.unapply)
  )

  /*
  Add new user function. BindFromRequest method searches the request 
  parameters for ones named after the form’s fields and uses them as 
  those fields values. Validation happens at binding. 
  If errors, redirects to add page. 
  if success, adds user to db. Db adds unique Id to user, which in this
  point is '-1'. Find same user by name and redirect to userpage.
  */

  def adduser = Action { implicit request =>
    print("addUser ")
    val newUserForm = userForm.bindFromRequest()
    newUserForm.fold(
      hasErrors = { form =>
        BadRequest(views.html.admin.addUser(form)(views.html.admin.users(User.findAll))).flashing(
            "error" -> "Something went wrong, maybe username is taken or passwords didnt match ")},
      success = { user =>
        println("success.")
        User.addUser(user.username, user.userpassword)
        Ok(views.html.admin.addUser(userForm)(views.html.admin.users(User.findAll))).flashing(
            "success" -> "New user added")
      } 
    )
  }

  def newUser = Action { implicit request =>
    val users = User.findAll
    val form = 
    if (flash.get("error").isDefined) {
        userForm.bind(flash.data)
    } else userForm

    Ok(views.html.admin.addUser(form)(views.html.admin.users(users)))
  }
  def deleteUser(name: String) = Action {
    User.delete(name)
    Redirect(routes.Admin.newUser)
  }

  /*
  Genre form : add and delete functions
  */

  val genreForm: Form[Genre] = Form(
    mapping(
      "genre" -> text.verifying(
        "Not unique genre", Genre.findByGenre(_).isEmpty)
    )(Genre.apply)(Genre.unapply)
  )

  def addgenre = Action { implicit request =>
    val newGenreForm = genreForm.bindFromRequest()
    newGenreForm.fold(
      hasErrors = { form =>
        BadRequest(views.html.admin.editGenre(form)(views.html.admin.genres(Genre.allSorted))).flashing(
            "error" -> "Something went wrong, maybe name taken")},
      success = { genre =>
        Genre.addGenre(genre.title)
        Ok(views.html.admin.editGenre(genreForm)(views.html.admin.genres(Genre.allSorted))).flashing(
            "success" -> "New genre added")
      } 
    )
  }

  def showGenres = Action { implicit request =>
    val form = 
    if (flash.get("error").isDefined) {
        genreForm.bind(flash.data)
    } else genreForm

    Ok(views.html.admin.editGenre(form)(views.html.admin.genres(Genre.allSorted)))
  }

  def deleteGenre(title: String) = Action {
    Genre.delete(title)
    Redirect(routes.Admin.showGenres)
  }

  /*
  Movie functions
  -form
  -addmovie = Action
  -showmovie = Action
  */

  val movieForm: Form[Movie] = Form(
    mapping(
      "title" -> text,
      "link" -> text,
      "coverimg" -> text
    )(Movie.apply)(Movie.unapply)
  )


  def addmovie = Action { implicit request =>
    val newMovieForm = movieForm.bindFromRequest()
    newMovieForm.fold(
      hasErrors = { form =>
        println("movie has errors")
        BadRequest(views.html.admin.addMovie(form)(Genre.allSorted)(views.html.admin.movies(Movie.findAll))).flashing(
            "error" -> "Something went wrong, maybe name taken")},
      success = { movie =>

        Movie.addMovie(movie.title, movie.link, movie.coverImg)
        Ok(views.html.admin.addMovie(movieForm)(Genre.allSorted)(views.html.admin.movies(Movie.findAll))).flashing(
            "success" -> "New movie added")
      } 
    )
  }

  def showMovie = Action { implicit request =>
    val form = 
    if (flash.get("error").isDefined) {
        movieForm.bind(flash.data)
    } else movieForm
    Ok(views.html.admin.addMovie(form)(Genre.allSorted)(views.html.admin.movies(Movie.findAll)))
  }
}