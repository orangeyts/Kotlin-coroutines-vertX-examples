drop table if exists V_MOVIE;
drop table if exists V_RATING;
CREATE TABLE V_MOVIE (ID VARCHAR(16) PRIMARY KEY, TITLE VARCHAR(256) NOT NULL);
CREATE TABLE V_RATING (ID INTEGER PRIMARY KEY auto_increment, value INTEGER, MOVIE_ID VARCHAR(16));
INSERT INTO V_MOVIE (ID, TITLE) VALUES ('starwars', 'Star Wars');
INSERT INTO V_MOVIE (ID, TITLE) VALUES ('indianajones', 'Indiana Jones');
INSERT INTO V_RATING (ID,VALUE, MOVIE_ID) VALUES (null,1, 'starwars');
INSERT INTO V_RATING (ID,VALUE, MOVIE_ID) VALUES (null,5, 'starwars');
INSERT INTO V_RATING (ID,VALUE, MOVIE_ID) VALUES (null,9, 'starwars');
INSERT INTO V_RATING (ID,VALUE, MOVIE_ID) VALUES (null,10, 'starwars');
INSERT INTO V_RATING (ID,VALUE, MOVIE_ID) VALUES (null,4, 'indianajones');
INSERT INTO V_RATING (ID,VALUE, MOVIE_ID) VALUES (null,7, 'indianajones');
INSERT INTO V_RATING (ID,VALUE, MOVIE_ID) VALUES (null,3, 'indianajones');
INSERT INTO V_RATING (ID,VALUE, MOVIE_ID) VALUES (null,9, 'indianajones');
SELECT * from V_MOVIE;
SELECT * from V_RATING;


-- 模拟一个大量数据的表
CREATE TABLE slow (name INTEGER, native VARCHAR(16));
INSERT INTO slow (name, native) VALUES ('name', '国籍');

-- 循环执行下面的语句  增加到百万级的数据
insert into slow (select * from slow);

-- 根据名字排序 全表扫描
EXPLAIN SELECT * from slow ORDER BY name;