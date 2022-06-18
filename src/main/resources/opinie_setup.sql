/*
    1. this script is created for SETUP purposes only, it creates schema from scratch and inserts all possible data
    2. it is very inaccurate to use it in case of data update
    3. use it only when migrating, replicating or creating schema from scratch

    In compliance with its destiny, running this script on the database which contains actual schema will result in an error :)
*/

/*
    Most of the fields are set to default null, it comes from the structure of our data,
        it is very messy so any serious constraints my cause a disaster
*/

/*
    MEDIUMINT - enough for the job, SMALLINT is too small (32k is the limit)

    FLOAT without defined precion as FLOAT(M, D) -
        because precision is deprecated and FLOAT is enough four our values

    TEXT - 65,5.. k characters is quite much but the previous tier is 255 chars so it is a must in this case
        even though it is redundant
*/
#START TRANSACTION;

CREATE TABLE teacher(
    teacherId MEDIUMINT PRIMARY KEY AUTO_INCREMENT,
    category VARCHAR(30) NOT NULL,
    full_name VARCHAR(80) DEFAULT NULL,
    academic_title VARCHAR(30) DEFAULT NULL,
    average_rating FLOAT,
    details_link VARCHAR(50) NOT NULL
);

CREATE TABLE review(
    reviewId MEDIUMINT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(60) DEFAULT NULL,
    given_rating FLOAT,
    title VARCHAR(100) DEFAULT NULL,
    review TEXT DEFAULT NULL,
    reviewer VARCHAR(30) DEFAULT NULL,
    post_date DATETIME,
    teacher_id MEDIUMINT NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacherId)
);

INSERT INTO teacher(category, full_name, academic_title, average_rating, details_link) VALUES('matematycy', 'Bogdan Krzysztof', 'dr', 4.77, 't,bogdan-krzysztof-dr,78')
