package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Flash
import play.api.data._
import play.api.data.Form._
import play.api.data.Forms.{mapping, number, nonEmptyText, boolean, tuple, text, seq, list, optional, ignored}
import play.api.mvc.{Action, Controller}

import anorm._
import anorm.SqlParser._

import models._

object Admin extends Controller with Secured{

  /*
  Dashboard renderer @(user:String)(content: Html)(right: Html)(implicit flash: Flash)
  uses "withAuth" action to authenticate user cookie
  */

	def dashboard = withAuth { username => implicit request =>
    val users = User.findAllSQL
    Ok(views.html.admin.dashboard(Auth.username(request).getOrElse(null))(null)(views.html.admin.users(users)))
	}

  /*
	Renders userpage or return 404
	*/  

  def showUser(id: Int) = withAuth { username => implicit request =>
      User.findById(id).map {user =>
	  		Ok(views.html.admin.user(Auth.username(request).getOrElse(null))(user))
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
  those fields values. Validation happens in binding. 
  If errors, redirects to add page. 
  if success, adds user to db. Db adds unique Id to user, which in this
  point is '-1'. Find same user by name and redirect to userpage.
  */

  def adduser = withAuth { username =>  implicit request =>
    print("addUser ")
    val newUserForm = userForm.bindFromRequest()
    newUserForm.fold(
      hasErrors = { form =>

        //@(userForm: Form[Aspirant])(RightHand: Html)(user: User)(implicit flash: Flash)
        BadRequest(views.html.admin.addUser(Auth.username(request).getOrElse(null))(form)(views.html.admin.users(User.findAll))).flashing(
            "error" -> "Something went wrong, maybe username is taken or passwords didnt match ")},
      success = { user =>
        println("success.")
        User.addUser(user.username, user.userpassword)
        Ok(views.html.admin.addUser(Auth.username(request).getOrElse(null))(userForm)(views.html.admin.users(User.findAll))).flashing(
            "success" -> "New user added")
      } 
    )
  }

  def newUser = withAuth { username => implicit request =>
    val users = User.findAll
    val form = 
    if (flash.get("error").isDefined) {
        userForm.bind(flash.data)
    } else userForm

    Ok(views.html.admin.addUser(Auth.username(request).getOrElse(null))(form)(views.html.admin.users(users)))
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
      "id" -> ignored(NotAssigned: anorm.Pk[Int]),
      "genre" -> text.verifying(
        "Not unique genre", Genre.findByGenre(_).isEmpty)
    )(Genre.apply)(Genre.unapply)
  )

  def addgenre = withAuth { username => implicit request =>
    val newGenreForm = genreForm.bindFromRequest()
    newGenreForm.fold(
      hasErrors = { form =>
        BadRequest(views.html.admin.editGenre(Auth.username(request).getOrElse(null))(form)(views.html.admin.genres(Genre.allSorted))).flashing(
            "error" -> "Something went wrong, maybe name taken")},
      success = { genre =>
        Genre.addGenre(genre.title)
        Ok(views.html.admin.editGenre(Auth.username(request).getOrElse(null))(genreForm)(views.html.admin.genres(Genre.allSorted))).flashing(
            "success" -> "New genre added")
      } 
    )
  }

  def showGenres = withAuth { username => implicit request =>
    val form = 
    if (flash.get("error").isDefined) {
        genreForm.bind(flash.data)
    } else genreForm

    Ok(views.html.admin.editGenre(Auth.username(request).getOrElse(null))(form)(views.html.admin.genres(Genre.allSorted)))
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

    //maps form content
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Int]),
      "title" -> nonEmptyText.verifying(
        "Not a unique name", Movie.findByName(_).isEmpty),
      "link" -> text,
      "coverimg" -> text,
      "genres[]" -> list(text),
      "plot" -> optional(text),
      "year" -> optional(number)
    )(Movie.apply)(Movie.unapply)
  )



  def addmovie = Action { implicit request =>
    val newMovieForm = movieForm.bindFromRequest()
    newMovieForm.fold(

      hasErrors = { form =>

        BadRequest(views.html.admin.addMovie((Auth.username(request).getOrElse(null)))(form)(Genre.allSorted)(views.html.admin.movies(Movie.findAll))).flashing(
            "error" -> "Something went wrong")},

      success = { movie =>
        Movie.addMovie(movie)
        movie.genres.foreach(println)
        Genres.addGenresToMovie(movie.genres, Movie.getID(movie.title))
        Ok(views.html.admin.addMovie(Auth.username(request).getOrElse(null))(movieForm)(Genre.allSorted)(views.html.admin.movies(Movie.findAll))).flashing(
            "success" -> "New movie added")
      } 
    )
  }

  def showMovie = withAuth { username => implicit request =>
    val form = 
    if (flash.get("error").isDefined) {
      movieForm.bind(flash.data)
    }
    else movieForm
    Ok(views.html.admin.addMovie(Auth.username(request).getOrElse(null))(form)(Genre.allSorted)(views.html.admin.movies(Movie.findAll)))
  }

  def edit(id:Int)  = withAuth { username => implicit request =>

    Ok(views.html.admin.editMovie(Auth.username(request).getOrElse(null))(Movie.findById(id).getOrElse(null))(Genre.allSorted)(views.html.admin.movies(Movie.findAll)))
  
  }
}