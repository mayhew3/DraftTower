
-- DRAFT QUERIES

CREATE OR REPLACE VIEW projectionsAll AS
(SELECT PlayerID, 'Pitcher' AS Role,
  NULL AS OBP,
  NULL AS SLG,
  NULL AS RHR,
  NULL AS RBI,
  NULL AS HR,
  NULL AS SBC,
  ROUND(INN, 1) AS INN, ROUND(ERA, 2) AS ERA, ROUND(WHIP, 3) AS WHIP, WL, K, S, Rank, DataSource, Rating, Draft
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
  Rank, DataSource, Rating, Draft
FROM projectionsBatting)
ORDER BY Rank;

CREATE OR REPLACE VIEW projectionsView AS
SELECT p.PlayerString as Player, p.FirstName, p.LastName, p.MLBTeam, p.Eligibility, 
 CASE Eligibility WHEN '' THEN 'DH' WHEN NULL THEN 'DH' ELSE Eligibility END as Position, 
 ds.name as Source,
 CASE WHEN p.ID IN (SELECT PlayerID FROM Keepers) THEN 1 ELSE 0 END AS Keeper,
 CASE WHEN p.ID IN (SELECT PlayerID FROM DraftResults WHERE BackedOut = 0) THEN 1 ELSE 0 END AS Drafted,
 p.Injury,
 pa.*
FROM projectionsAll pa
INNER JOIN Players p
 ON pa.PlayerID = p.ID
INNER JOIN data_sources ds
 ON pa.DataSource = ds.ID;

CREATE OR REPLACE VIEW rankingsView AS
SELECT p.PlayerString as Player, p.FirstName, p.LastName, p.MLBTeam, p.Eligibility, 
 CASE Eligibility WHEN '' THEN 'DH' WHEN NULL THEN 'DH' ELSE Eligibility END as Position, 
 CASE WHEN p.ID IN (SELECT PlayerID FROM Keepers) THEN 1 ELSE 0 END AS Keeper,
 CASE WHEN p.ID IN (SELECT PlayerID FROM DraftResults WHERE BackedOut = 0) THEN 1 ELSE 0 END AS Drafted,
 p.Injury,
 cr.TeamID,
 cr.Rank,
 pa.PlayerID,
 pa.OBP, pa.SLG, pa.RHR, pa.RBI, pa.HR, pa.SBC,
 pa.ERA, pa.WHIP, pa.K, pa.WL, pa.S, pa.INN, pa.Rating
FROM projectionsAll pa
INNER JOIN Players p
 ON pa.PlayerID = p.ID
INNER JOIN customrankings cr
 on cr.playerid = p.ID
WHERE pa.DataSource = 2;

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



