
-- DRAFT QUERIES

CREATE OR REPLACE VIEW projectionsAll AS
(SELECT PlayerID, 'Pitcher' AS Role,
  NULL AS OBP,
  NULL AS SLG,
  NULL AS RHR,
  NULL AS RBI,
  NULL AS HR,
  NULL AS SBC,
  ROUND(INN, 1) AS INN, ROUND(ERA, 2) AS ERA, ROUND(WHIP, 3) AS WHIP, WL, K, S, Rank, DataSource, Rating
FROM projectionsPitching)
UNION
(SELECT PlayerID, 'Batter' AS Role,
  ROUND(OBP, 3) AS OBP, ROUND(SLG, 3) AS SLG, RHR, RBI, HR, SBC,
  NULL AS INN,
  NULL AS ERA,
  NULL AS WHIP,
  NULL AS WL,
  NULL AS K,
  NULL AS S,
  Rank, DataSource, Rating
FROM projectionsBatting)
ORDER BY Rank;

CREATE OR REPLACE VIEW projectionsView AS
SELECT p.PlayerString as Player, p.FirstName, p.LastName, p.MLBTeam, p.Eligibility, p.Eligibility as Position, ds.name as Source,
 CASE WHEN p.ID IN (SELECT PlayerID FROM Keepers) THEN 1 ELSE 0 END AS Keeper,
 CASE WHEN p.ID IN (SELECT PlayerID FROM DraftResults WHERE BackedOut = 0) THEN 1 ELSE 0 END AS Drafted,
 pa.*
FROM projectionsAll pa
INNER JOIN Players p
 ON pa.PlayerID = p.ID
INNER JOIN data_sources ds
 ON pa.DataSource = ds.ID;


-- start of aggregated projections, not done yet.
CREATE OR REPLACE VIEW projectionBatAggr AS
SELECT PlayerID,
 AVG(Rank) AS Rank,
 COUNT(1) AS Sources,
 AVG(`1B`) AS `1B`,
 AVG(`2B`) AS `2B`,
 AVG(`3B`) AS `3B`,
 AVG(OBP) AS OBP,
 AVG(SLG) AS SLG
FROM projBatCalc
GROUP BY PlayerID
ORDER BY AVG(Rank);



