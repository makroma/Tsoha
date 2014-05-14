

-- -----------------------------------------------------
-- TABLE Movies  
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS Movies (
    MovieId   SERIAL NOT NULL, --unique ID
    GenreId   INT NULL,
    MovieTitle   VARCHAR(200) NULL,
    Year   INT NULL,
    Plot   VARCHAR(800) NULL,
    Lenght   TIME NULL,
    AddingDate   DATE NULL,
    Director   VARCHAR(45) NULL,
    Writer   VARCHAR(45) NULL,
    Actors   VARCHAR(300) NULL,
    Link   VARCHAR(200) NULL,
    CoverImg   VARCHAR(200) NULL,
  PRIMARY KEY (  MovieId  )
);


-- -----------------------------------------------------
-- TABLE Genres  
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS Genres   (
    GenreId   SERIAL NOT NULL,
    GenreName   VARCHAR(45) NULL,
  PRIMARY KEY (  GenreId  )
);


-- -----------------------------------------------------
-- TABLE Genres_has_Movies  - Intersection table
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS Genres_has_Movies   (
    Genres_GenreId   SERIAL NOT NULL,
    Movies_GenreId   SERIAL NOT NULL,
  PRIMARY KEY (  Genres_GenreId  ,   Movies_GenreId  ),
  CONSTRAINT   fk_Genres_has_Movies_Genres  
    FOREIGN KEY (  Genres_GenreId  )
    REFERENCES   Genres   (  GenreId  )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT   fk_Genres_has_Movies_Movies1  
    FOREIGN KEY (  Movies_GenreId  )
    REFERENCES   Movies   (  MovieId  )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);


-- -----------------------------------------------------
-- TABLE Users  
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS  Users   (
    UserId   SERIAL NOT NULL,
    UserName   VARCHAR(45) NULL,
    UserPassword   VARCHAR(45) NULL,
    Admin   BOOLEAN NULL,
  PRIMARY KEY (  UserId  )
 );


-- -----------------------------------------------------
-- TABLE Grades  
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS Grades   (
    Users_UserId   SERIAL NOT NULL,
    Movies_MovieId   SERIAL NOT NULL,
    Stars   INT NULL,
  PRIMARY KEY (  Users_UserId  ,   Movies_MovieId  ),
  CONSTRAINT   fk_Users_has_Movies_Users1  
    FOREIGN KEY (  Users_UserId  )
    REFERENCES   Users   (  UserId  )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT   fk_Users_has_Movies_Movies1  
    FOREIGN KEY (  Movies_MovieId  )
    REFERENCES   Movies   (  MovieId  )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);


-- -----------------------------------------------------
-- TABLE Watchlist  
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS Watchlist   (
    Users_UserId   SERIAL NOT NULL,
    Movies_MovieId  SERIAL NOT NULL,
  PRIMARY KEY (  Users_UserId  ,   Movies_MovieId  ),
  CONSTRAINT   fk_Watchlist_Users1  
    FOREIGN KEY (  Users_UserId  )
    REFERENCES   Users   (  UserId  )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT   fk_Watchlist_Movies1  
    FOREIGN KEY (  Movies_MovieId  )
    REFERENCES   Movies   (  MovieId  )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

