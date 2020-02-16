DROP VIEW IF EXISTS q0, q1i, q1ii, q1iii, q1iv, q2i, q2ii, q2iii, q3i, q3ii, q3iii, q4i, q4ii, q4iii, q4iv, q4v;

-- Question 0
CREATE VIEW q0(era) 
AS
  SELECT MAX(era) -- replace this line
  FROM pitching
;

-- Question 1i
CREATE VIEW q1i(namefirst, namelast, birthyear)
AS
  SELECT namefirst, namelast, birthyear -- replace this line
  FROM people
  WHERE weight > 300
;

-- Question 1ii
CREATE VIEW q1ii(namefirst, namelast, birthyear)
AS
  SELECT namefirst, namelast, birthyear -- replace this line
  FROM people 
  WHERE namefirst LIKE '% %'
;

-- Question 1iii
CREATE VIEW q1iii(birthyear, avgheight, count)
AS
  SELECT birthyear, AVG(height) AS avgheight, COUNT(*) as count -- replace this line
  FROM people
  GROUP BY birthyear
  ORDER BY birthyear
;

-- Question 1iv
CREATE VIEW q1iv(birthyear, avgheight, count)
AS
  SELECT birthyear, AVG(height) AS avgheight, COUNT(*) as count -- replace this line
  FROM people
  GROUP BY birthyear
  HAVING AVG(height) > 70
  ORDER BY birthyear ASC
;

-- Question 2i
CREATE VIEW q2i(namefirst, namelast, playerid, yearid)
AS
  SELECT P.namefirst, P.namelast, P.playerid, H.yearid -- replace this line
  FROM people P, halloffame H
  WHERE P.playerid = H.playerid AND H.inducted = 'Y'
  ORDER BY H.yearid DESC
;

-- Question 2ii
CREATE VIEW q2ii(namefirst, namelast, playerid, schoolid, yearid)
AS
  SELECT P.namefirst, P.namelast, P.playerid, S.schoolid, H.yearid -- replace this line
  FROM people P 
  INNER JOIN halloffame H ON P.playerid = H.playerid
  INNER JOIN collegeplaying C ON P.playerid = C.playerid
  INNER JOIN schools S ON C.schoolid = S.schoolid
  WHERE H.inducted = 'Y' AND S.schoolstate = 'CA'
  ORDER BY H.yearid DESC, S.schoolid ASC, P.playerid ASC
;

-- Question 2iii
CREATE VIEW q2iii(playerid, namefirst, namelast, schoolid)
AS
  SELECT P.playerid, P.namefirst, P.namelast, C.schoolid -- replace this line
  FROM people P
  INNER JOIN halloffame H ON P.playerid = H.playerid
  LEFT OUTER JOIN collegeplaying C ON P.playerid = C.playerid
  WHERE H.inducted = 'Y'
  ORDER BY P.playerid DESC, P.playerid ASC, C.schoolid
;

-- Question 3i
CREATE VIEW q3i(playerid, namefirst, namelast, yearid, slg)
AS
  SELECT P.playerid, P.namefirst, P.namelast, B.yearid, 
  ((B.h - B.h2b - B.h3b - B.hr + 2*B.h2b + 3*B.h3b + 4*B.hr) 
        / (cast(B.ab AS real))) AS slg -- replace this line
  FROM people P INNER JOIN batting B ON P.playerid = B.playerid
  WHERE B.ab > 50
  ORDER BY slg DESC, B.yearid ASC, P.playerid ASC
  LIMIT 10
;

-- Question 3ii
CREATE VIEW q3ii(playerid, namefirst, namelast, lslg)
AS
  SELECT P.playerid, P.namefirst, P.namelast, 
  (SUM(B.h - B.h2b - B.h3b - B.hr + 2*B.h2b + 3*B.h3b + 4*B.hr) 
        / (cast(SUM(B.ab) as real))) AS lslg -- replace this line
  FROM people P INNER JOIN batting B ON P.playerid = B.playerid
  WHERE b.ab > 0
  GROUP BY P.playerid
  HAVING SUM(B.ab) > 50
  ORDER BY lslg DESC, P.playerid ASC
  LIMIT 10
; 

-- Question 3iii
CREATE VIEW q3iii(namefirst, namelast, lslg)
AS
  WITH X(playerid, lslg) AS
  (SELECT P.playerid, 
  (SUM(B.h - B.h2b - B.h3b - B.hr + 2*B.h2b + 3*B.h3b + 4*B.hr) 
        / (cast(SUM(B.ab) as real))) AS lslg -- replace this line
  FROM people P INNER JOIN batting B ON P.playerid = B.playerid
  WHERE b.ab > 0
  GROUP BY P.playerid
  HAVING SUM(B.ab) > 50)

  SELECT P.namefirst, P.namelast, X.lslg
  FROM people P INNER JOIN X ON P.playerid = X.playerid
  WHERE X.lslg > (SELECT lslg FROM X WHERE playerid = 'mayswi01')

;

-- Question 4i
CREATE VIEW q4i(yearid, min, max, avg, stddev)
AS
  SELECT yearid, MIN(salary) AS min, MAX(salary) AS max, AVG(salary) AS avg, STDDEV(salary) AS stddev -- replace this line
  FROM salaries
  GROUP BY yearid
  ORDER BY yearid ASC
;

-- Question 4ii
CREATE VIEW q4ii(binid, low, high, count)
AS
  WITH X(min, max) AS (SELECT MIN(salary), MAX(salary) 
  						FROM salaries 
  						WHERE yearid = 2016), 
  	   Y(binid, minR, maxR) AS (SELECT i AS binid, 
  	   								   i * (X.max - X.min) / 10 + X.min AS minR,
  	   								   (i + 1) * (X.max - X.min) / 10 + X.min AS maxR
  	   							FROM generate_series(0,9) AS i, X)

  SELECT Y.binid AS binid, Y.minR AS low, Y.maxR AS high, COUNT(*) -- replace this line
  FROM Y INNER JOIN salaries S 
  ON (S.salary >= Y.minR AND (S.salary < Y.maxR OR (Y.binid = 9 AND S.salary <= Y.maxR)) AND S.yearid = 2016)
  GROUP BY binid, low, high
  ORDER BY Y.binid
;
-- Question 4iii
CREATE VIEW q4iii(yearid, mindiff, maxdiff, avgdiff)
AS
  WITH 
  Before(yearid, min, max, avg) AS 
  	(SELECT yearid, MIN(salary), MAX(salary), AVG(salary)
  	 FROM salaries 
  	 GROUP BY yearid),
  After(yearid, min, max, avg) AS 
    (SELECT yearid, MIN(salary), MAX(salary), AVG(salary)
     FROM salaries
     GROUP BY yearid),
  Diff AS (SELECT B.yearid AS yearidBefore, A.yearid AS yearidAfter, 
  	   		  B.min AS minBefore, A.min AS minAfter,
  	   	      B.max AS maxBefore, A.max AS maxAfter,
  	   		  B.avg AS avgBefore, A.avg AS avgAfter
  	   FROM Before B, After A 
  	   WHERE A.yearid = B.yearid + 1)

  SELECT D.yearidAfter as yearid, 
  		(D.minAfter - D.minBefore) AS mindiff, 
  		(D.maxAfter - D.maxBefore) AS maxdiff, 
  		(D.avgAfter - D.avgBefore) AS avgdiff -- replace this line
  FROM Diff AS D
  ORDER BY yearid ASC
;

-- Question 4iv
CREATE VIEW q4iv(playerid, namefirst, namelast, salary, yearid)
AS
  WITH X AS (SELECT MAX(salary) 
			FROM salaries
			WHERE yearid = 2000),
  	   Y AS (SELECT MAX(salary)
  	   		FROM salaries
  	   		WHERE yearid = 2001)

  SELECT P.playerid, P.namefirst, P.namelast, S.salary, S.yearid -- replace this line
  FROM people P INNER JOIN salaries S ON (P.playerid = S.playerid)
  WHERE (S.yearid = 2000 AND S.salary >= (SELECT * FROM X)
      OR S.yearid = 2001 AND S.salary >= (SELECT * FROM Y))
;
-- Question 4v
CREATE VIEW q4v(team, diffAvg) 
AS
  SELECT a.teamid as team, MAX(salary) - MIN(salary)
  FROM allstarfull AS a 
  INNER JOIN salaries AS s
  ON a.playerid = s.playerid and a.yearid = s.yearid
  WHERE s.yearid = 2016
  GROUP BY team
  ORDER BY team
;

