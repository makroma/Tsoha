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
import scala.collection.mutable.ArrayBuffer

import models._

object Admin extends Controller with Secured{

  /*
  Dashboard renderer @(user:String)(user:String)(content: Html)(right: Html)(implicit flash: Flash)
  uses "withAuth" action to authenticate user cookie
  */

  def dashboard = withAdmin { user => implicit request =>

    val users = User.findAllSQL
    Ok(views.html.admin.dashboard(Auth.username(request).getOrElse(null))(null)(views.html.admin.users(users)))
  }

  /*
  Renders userpage or return 404
  */  

  def showUser(id: Int) = withAdmin { user => implicit request =>
    User.findById(id).map { user =>
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
  if success, adds user to db. Db adds unique Id to user. 
  Find same user by name and redirect to userpage.
  */

  def adduser = withAdmin { user =>  implicit request =>
    print("addUser ")
    val newUserForm = userForm.bindFromRequest()
    newUserForm.fold(
      hasErrors = { form =>

        //@(userForm: Form[Aspirant])(RightHand: Html)(user: User)(implicit flash: Flash)
        val flash = play.api.mvc.Flash(Map("error" -> "Something went wrong, maybe passwords did not match"))
        BadRequest(views.html.admin.addUser(Auth.username(request).getOrElse(null))(form)(views.html.admin.users(User.findAll))(flash))},
      success = { user =>
        println("success.")
        User.addUser(user.username, user.userpassword)
        val flash = play.api.mvc.Flash(Map("success" -> "New user added"))
        Ok(views.html.admin.addUser(Auth.username(request).getOrElse(null))(userForm)(views.html.admin.users(User.findAll))(flash))
      } 
    )
  }

  def newUser = withAdmin { user => implicit request =>
    val users = User.findAll
    val form = 
      if (flash.get("error").isDefined) {
          userForm.bind(flash.data)
      } else userForm

    Ok(views.html.admin.addUser(Auth.username(request).getOrElse(null))(form)(views.html.admin.users(users)))
  }

  def deleteUser(name: String) = withAdmin { user => implicit request =>
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

  def addgenre = withAdmin { user => implicit request =>
    val newGenreForm = genreForm.bindFromRequest()
    newGenreForm.fold(
      hasErrors = { form =>
        val flash = play.api.mvc.Flash(Map("error" -> "Genre name is taken"))
        BadRequest(views.html.admin.editGenre(Auth.username(request).getOrElse(null))(form)(views.html.admin.genres(Genre.allSorted))(flash))},
      success = { genre =>
        Genre.addGenre(genre.title)
        val flash = play.api.mvc.Flash(Map("success" -> "New genre added"))
        Ok(views.html.admin.editGenre(Auth.username(request).getOrElse(null))(genreForm)(views.html.admin.genres(Genre.allSorted))(flash))
      } 
    )
  }

  def showGenres = withAdmin { user => implicit request =>
    val form = 
    if (flash.get("error").isDefined) {
        genreForm.bind(flash.data)
    } else genreForm

    Ok(views.html.admin.editGenre(Auth.username(request).getOrElse(null))(form)(views.html.admin.genres(Genre.allSorted)))
  }

  def deleteGenre(title: String) = withAdmin { user => implicit request =>
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
      "link" -> optional(text),
      "coverimg" -> optional(text),
      "genres" -> list(text),
      "plot" -> optional(text),
      "year" -> optional(number)
    )(Movie.apply)(Movie.unapply)
  )

  /**
  * Add movie. Fold form. 
  * In case of error, will iterate thrue form values and add them "selectedGenres" list. Which is then passed
  * to view as constructor parameter.  
  * If success, will add movie to db and attach genres to movie. Then redirect to addmovie page
  * 
  */


  def addmovie = withAdmin { user => implicit request =>
    val newMovieForm = movieForm.bindFromRequest()
    newMovieForm.fold(

      hasErrors = { form =>

        val selectedGenre = ArrayBuffer[String]()
        form.data.values.iterator.foreach( i => if(!i.isEmpty) selectedGenre += i )

        val flash = play.api.mvc.Flash(Map( "error" -> "Something went wrong"))

        BadRequest(
          views.html.admin.addMovie(

            /* Contructor params */
            (Auth.username(request).getOrElse(null)))
            (form)
            (Genre.allSorted, selectedGenre.toList)
            (views.html.admin.movies(Movie.findAll))
            (flash)
        )
      },

      success = { movie =>

        Movie.addMovie(movie)
        Genres.addGenresToMovie(movie.genres, Movie.getID(movie.title))

        val flash = play.api.mvc.Flash(Map( "success" -> "New movie added"))

        Ok(views.html.admin.addMovie(
          /* Contructor params */
          Auth.username(request).getOrElse(null))
          (movieForm)
          (Genre.allSorted, null)
          (views.html.admin.movies(Movie.findAll))
          (flash)
        )
      } 
    )
  }

  def showMovie = withAdmin { user => implicit request =>
    val form = 
      if (flash.get("error").isDefined) {
        movieForm.bind(flash.data)
      } else movieForm

      var selectedGenre = ArrayBuffer[String]()
    
    Ok(views.html.admin.addMovie(Auth.username(request).getOrElse(null))(form)(Genre.allSorted, selectedGenre.toList)(views.html.admin.movies(Movie.findAll)))
  }

  /*
  * Edit movie functions
  * "edit" for page Action 
  */

  def edit(id:Int) = withAdmin { user => implicit request =>

    val movie = Movie.findById(id).getOrElse(null)
    /*Attach genres list to the Movie*/
    movie.genres = Genres.getMovieGenres(movie.title)

    val form:Form[Movie] = movieForm.fill(movie)
    
    Ok(views.html.admin.editMovie(
      /*Constructor params*/
      Auth.username(request).getOrElse(null), form, movie)
      (Genre.allSorted)
      (views.html.admin.movies(Movie.findAll)))
  }

  def updateMovie = withAdmin { user => implicit request =>
    val newMovieForm = movieForm.bindFromRequest()
    newMovieForm.fold(

      hasErrors = { form =>

        val selectedGenre = ArrayBuffer[String]()
        form.data.values.iterator.foreach( i => if(!i.isEmpty) selectedGenre += i )

        val flash = play.api.mvc.Flash(Map( "error" -> "Something went wrong"))

        BadRequest(
          views.html.admin.addMovie(

            /* Contructor params */
            (Auth.username(request).getOrElse(null)))
            (form)
            (Genre.allSorted, selectedGenre.toList)
            (views.html.admin.movies(Movie.findAll))
            (flash)
        )
      },

      success = { movie =>

        Movie.addMovie(movie)
        Genres.addGenresToMovie(movie.genres, Movie.getID(movie.title))

        val flash = play.api.mvc.Flash(Map( "success" -> "Movie info updated!"))

        Ok(views.html.admin.addMovie(
          /* Contructor params */
          Auth.username(request).getOrElse(null))
          (movieForm)
          (Genre.allSorted, null)
          (views.html.admin.movies(Movie.findAll))
          (flash)
        )
      } 
    )
  }

  def deleteMovie(movie: String) = withAdmin { user => implicit request =>
    Movie.delete(movie)
    Redirect(routes.Admin.showMovie).flashing(
      "success" -> "Movie deleted!")
  }
}