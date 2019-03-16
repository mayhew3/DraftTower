
-- DRAFT QUERIES

CREATE OR REPLACE VIEW YearlyBatScoring AS
SELECT *,
  (H+BB+IFNULL(HP, 0))/(AB+BB+IFNULL(HP, 0)) AS OBP,
  (1B+2*2B+3*3B+4*HR)/AB AS SLG,
  (SB-CS) AS SBC,
  (R-HR) AS RHR,
  (1B+2*2B+3*3B+4*HR) AS TB,
  (H+1216+BB+489+IFNULL(HP, 0))/(AB+4284+BB+489+IFNULL(HP, 0)) AS TeamOBP,
  (1B+2*2B+3*3B+4*HR+2036)/(AB+4284) AS TeamSLG
FROM YearlyBatting;

CREATE OR REPLACE VIEW YearlyPitchScoring AS
SELECT *, (W-L) AS WL,
          OUTS/3 AS INN,
          HA+BBI+IFNULL(HB, 0) AS BRA,
          ER/OUTS*27 AS ERA,
          (HA+BBI)/(OUTS/3) AS WHIP,
          (ER+467)/(OUTS/3+1038)*9 AS TeamERA,
          ((HA+1023)+(BBI+329)) / (OUTS/3+1038) AS TeamWHIP
FROM YearlyPitching;

CREATE OR REPLACE VIEW StdDeviationPitching AS
SELECT 
  STDDEV_POP(INN) AS INN,
  STDDEV_POP(ER) AS ER,
  STDDEV_POP(BRA) AS BRA,
  STDDEV_POP(S) AS S,
  STDDEV_POP(WL) AS WL,
  STDDEV_POP(K) AS K,
  STDDEV_POP(ERA) AS ERA,
  STDDEV_POP(WHIP) AS WHIP,
  STDDEV_POP(TeamERA) AS TeamERA,
  STDDEV_POP(TeamWHIP) AS TeamWHIP
FROM YearlyPitchScoring
WHERE Role IN ('Starter', 'Closer');

CREATE OR REPLACE VIEW StdDeviationBatting AS
SELECT 
  STDDEV_POP(OBP) AS OBP,
  STDDEV_POP(SLG) AS SLG,
  STDDEV_POP(SBC) AS SBC,
  STDDEV_POP(RHR) AS RHR,
  STDDEV_POP(RBI) AS RBI,
  STDDEV_POP(HR) AS HR,
  STDDEV_POP(TB) AS TB,
  STDDEV_POP(TeamOBP) AS TeamOBP,
  STDDEV_POP(TeamSLG) AS TeamSLG
FROM YearlyBatScoring
WHERE AB > 400;


CREATE OR REPLACE VIEW AvgPitching AS
SELECT 
  AVG(INN) AS INN,
  AVG(ER) AS ER,
  AVG(BRA) AS BRA,
  AVG(S) AS S,
  AVG(WL) AS WL,
  AVG(K) AS K,
  AVG(ERA) AS ERA,
  AVG(WHIP) AS WHIP,
  AVG(TeamERA) AS TeamERA,
  AVG(TeamWHIP) AS TeamWHIP
FROM YearlyPitchScoring
WHERE Role IN ('Starter', 'Closer');


CREATE OR REPLACE VIEW AvgBatting AS
SELECT 
  AVG(OBP) AS OBP,
  AVG(SLG) AS SLG,
  AVG(SBC) AS SBC,
  AVG(RHR) AS RHR,
  AVG(RBI) AS RBI,
  AVG(HR) AS HR,
  AVG(TB) AS TB,
  AVG(TeamOBP) AS TeamOBP,
  AVG(TeamSLG) AS TeamSLG
FROM YearlyBatScoring
WHERE AB > 400;


CREATE OR REPLACE VIEW StdDeviationBattingByPos AS
SELECT Position,
  STDDEV_POP(OBP) AS OBP,
  STDDEV_POP(SLG) AS SLG,
  STDDEV_POP(SBC) AS SBC,
  STDDEV_POP(RHR) AS RHR,
  STDDEV_POP(RBI) AS RBI,
  STDDEV_POP(HR) AS HR,
  STDDEV_POP(TB) AS TB,
  STDDEV_POP(TeamOBP) AS TeamOBP,
  STDDEV_POP(TeamSLG) AS TeamSLG
FROM Eligibilities e
INNER JOIN YearlyBatScoring yb
 ON e.PlayerID = yb.PlayerID
WHERE AB > 400
GROUP BY Position;

CREATE OR REPLACE VIEW AvgBattingByPos AS
SELECT Position,
  AVG(OBP) AS OBP,
  AVG(SLG) AS SLG,
  AVG(SBC) AS SBC,
  AVG(RHR) AS RHR,
  AVG(RBI) AS RBI,
  AVG(HR) AS HR,
  AVG(TB) AS TB,
  AVG(TeamOBP) AS TeamOBP,
  AVG(TeamSLG) AS TeamSLG
FROM Eligibilities e
INNER JOIN YearlyBatScoring yb
 ON e.PlayerID = yb.PlayerID
WHERE AB > 400
GROUP BY Position;

CREATE OR REPLACE VIEW StdDeviationPitchingByPos AS
SELECT Role,
  STDDEV_POP(INN) AS INN,
  STDDEV_POP(ER) AS ER,
  STDDEV_POP(BRA) AS BRA,
  STDDEV_POP(WL) AS WL,
  STDDEV_POP(K) AS K,
  STDDEV_POP(S) AS S,
  STDDEV_POP(ERA) AS ERA,
  STDDEV_POP(WHIP) AS WHIP,
  STDDEV_POP(TeamERA) AS TeamERA,
  STDDEV_POP(TeamWHIP) AS TeamWHIP
FROM YearlyPitchScoring yb
WHERE OUTS > 120
GROUP BY Role;

CREATE OR REPLACE VIEW AvgPitchingByPos AS
SELECT Role,
  AVG(INN) AS INN,
  AVG(ER) AS ER,
  AVG(BRA) AS BRA,
  AVG(S) AS S,
  AVG(WL) AS WL,
  AVG(K) AS K,
  AVG(ERA) AS ERA,
  AVG(WHIP) AS WHIP,
  AVG(TeamERA) AS TeamERA,
  AVG(TeamWHIP) AS TeamWHIP
FROM YearlyPitchScoring yb
WHERE OUTS > 120
GROUP BY Role;

CREATE OR REPLACE VIEW AllPlayers AS
SELECT p.*,
	  CASE WHEN p.ID IN (SELECT PlayerID FROM DraftResults WHERE BackedOut = 0) THEN 1 ELSE 0 END AS Drafted,
      CASE WHEN k.ID IS NULL THEN 0 ELSE 1 END AS Keeper
FROM Players p
LEFT OUTER JOIN DraftResults dr
  ON dr.PlayerID = p.ID
LEFT OUTER JOIN Keepers k
  ON k.PlayerID = p.ID
LEFT OUTER JOIN YearlyBatting yb
  ON yb.PlayerID = p.ID
LEFT OUTER JOIN YearlyPitching yp
  ON yp.PlayerID = p.ID;


CREATE OR REPLACE VIEW TeamBatting AS
SELECT TeamID, MIN(TeamName), 
  SUM(RHR) AS RHR,
  SUM(RBI) AS RBI,
  SUM(SBC) AS SBC,
  SUM(HR) AS HR,
  SUM(TB)/SUM(AB) AS SLG,
  (SUM(H)+SUM(BB)+IFNULL(SUM(HP),0))/(SUM(AB)+IFNULL(SUM(HP),0)+IFNULL(SUM(SF),0)) AS OBP,
  COUNT(1) AS NumPlayers
FROM DraftResults dr
INNER JOIN YearlyBatScoring yb
 ON dr.PlayerID = yb.PlayerID
WHERE dr.BackedOut = 0
AND DraftPos <> 'RS'
GROUP BY dr.TeamID
ORDER BY dr.TeamID;

CREATE OR REPLACE VIEW TeamPitching AS
SELECT TeamID, MIN(TeamName), 
  SUM(INN) AS INN,
  SUM(ER) AS ER,
  SUM(BRA) AS BRA,
  SUM(WL) AS WL,
  SUM(K) AS K,
  SUM(S) AS S,
  SUM(ER)/SUM(INN)*9 AS ERA,
  (SUM(HA)+SUM(BBI))/(SUM(INN)) AS WHIP,
  COUNT(1) AS NumPlayers
FROM DraftResults dr
INNER JOIN YearlyPitchScoring yp
 ON dr.PlayerID = yp.PlayerID
WHERE dr.BackedOut = 0
AND DraftPos <> 'RS'
GROUP BY dr.TeamID
ORDER BY dr.TeamID;

CREATE OR REPLACE VIEW TeamScoring AS
(SELECT tb.TeamID AS TeamID, 
  OBP, SLG, RHR, RBI, HR, SBC, INN, ERA, WHIP, WL, K, S
FROM TeamBatting tb
LEFT OUTER JOIN TeamPitching tp
 ON tb.TeamID = tp.TeamID)
 UNION
 (SELECT tp.TeamID AS TeamID, 
  OBP, SLG, RHR, RBI, HR, SBC, INN, ERA, WHIP, WL, K, S
FROM TeamBatting tb
RIGHT OUTER JOIN TeamPitching tp
 ON tb.TeamID = tp.TeamID);

CREATE OR REPLACE VIEW BatterQualityByCat AS
SELECT yb.*, FirstName, LastName, MLBTeam, Drafted, Keeper, Injury, e.Position,
  (yb.TeamOBP-ab.TeamOBP)/std.TeamOBP AS OBPRating,
  (yb.TeamSLG-ab.TeamSLG)/std.TeamSLG AS SLGRating,
  (yb.RHR-ab.RHR)/std.RHR AS RHRRating,
  (yb.RBI-ab.RBI)/std.RBI AS RBIRating,
  (yb.HR-ab.HR)/std.HR AS HRRating,
  (yb.SBC-ab.SBC)/std.SBC AS SBCRating
FROM AvgBattingByPos AS ab
INNER JOIN StdDeviationBattingByPos AS std
 ON ab.Position = std.Position
INNER JOIN Eligibilities e
 ON ab.Position = e.Position
INNER JOIN YearlyBatScoring AS yb
 ON yb.PlayerID = e.PlayerID
INNER JOIN AllPlayers p
 ON yb.PlayerID = p.ID
WHERE AB > 300
ORDER BY Player, e.Position ASC;

CREATE OR REPLACE VIEW BatterQuality AS
SELECT *, (OBPRating + SLGRating + RHRRating + RBIRating + HRRating + SBCRating) AS Total
FROM BatterQualityByCat
ORDER BY (OBPRating + SLGRating + RHRRating + RBIRating + HRRating + SBCRating) DESC;

                  
CREATE OR REPLACE VIEW PitcherQualityByCat AS
SELECT yp.*, FirstName, LastName, MLBTeam, Drafted, Keeper, Injury,
  (yp.INN-ap.INN)/std.INN AS INNRating,
  (ap.TeamERA-yp.TeamERA)/std.TeamERA AS ERARating,
  (ap.TeamWHIP-yp.TeamWHIP)/std.TeamWHIP AS WHIPRating,
  (yp.WL-ap.WL)/std.WL AS WLRating,
  (yp.K-ap.K)/std.K AS KRating,
  (yp.S-ap.S)/std.S AS SRating
FROM AllPlayers p
INNER JOIN YearlyPitchScoring yp
 on yp.PlayerID = p.ID,
StdDeviationPitching AS std,
AvgPitching AS ap
WHERE yp.OUTS > 120
ORDER BY Player ASC;

CREATE OR REPLACE VIEW PitcherQuality AS
SELECT *, (INNRating + ERARating + WHIPRating + WLRating + KRating + SRating) AS Total
FROM PitcherQualityByCat
ORDER BY (INNRating + ERARating + WHIPRating + WLRating + KRating + SRating) DESC;

CREATE OR REPLACE VIEW AllPlayersByQuality AS
(SELECT Player, PlayerID, FirstName, LastName, MLBTeam, Rank, Total, 'P' AS Position
FROM PitcherQuality)
UNION
(SELECT Player, PlayerID, FirstName, LastName, MLBTeam, Rank, Total, Position
FROM BatterQuality)
ORDER BY Total DESC;

CREATE OR REPLACE VIEW UnclaimedDisplayPlayersByQuality AS
(SELECT Player, PlayerID, Rank, Total, 'P' AS Position
FROM PitcherQuality
WHERE Drafted = 0 AND Keeper = 0)
UNION
(SELECT Player, PlayerID, Rank, Total, Position
FROM BatterQuality
WHERE Drafted = 0 AND Keeper = 0)
ORDER BY Total DESC;

CREATE OR REPLACE VIEW UnclaimedDisplayPlayersWithCatsByQuality AS
(SELECT Player, PlayerID, 'P' AS Eligibility, Role,
  NULL AS OBP,
  NULL AS SLG,
  NULL AS RHR,
  NULL AS RBI,
  NULL AS HR,
  NULL AS SBC,
  ROUND(INN, 1) AS INN, ROUND(ERA, 2) AS ERA, ROUND(WHIP, 3) AS WHIP, WL, K, S, Rank, ROUND(Total, 3) AS Rating, 'P' AS Position,
  FirstName, LastName, MLBTeam, Injury
FROM PitcherQuality
WHERE Drafted = 0 AND Keeper = 0)
UNION
(SELECT Player, PlayerID, Eligibility, 'Batter' AS Role,
  ROUND(OBP, 3) AS OBP, ROUND(SLG, 3) AS SLG, RHR, RBI, HR, SBC,
  NULL AS INN,
  NULL AS ERA,
  NULL AS WHIP,
  NULL AS WL,
  NULL AS K,
  NULL AS S,
  Rank, ROUND(Total, 3) AS Rating, Position,
  FirstName, LastName, MLBTeam, Injury
FROM BatterQuality
WHERE Drafted = 0 AND Keeper = 0)
ORDER BY Rating DESC;

CREATE OR REPLACE VIEW TeamScoringWithZeroes AS
SELECT Teams.ID AS TeamID,
  COALESCE(OBP, 0) AS OBP,
  COALESCE(SLG, 0) AS SLG,
  COALESCE(RHR, 0) AS RHR,
  COALESCE(RBI, 0) AS RBI,
  COALESCE(HR, 0) AS HR,
  COALESCE(SBC, 0) AS SBC,
  COALESCE(INN, 0) AS INN,
  COALESCE(ERA, 0) AS ERA,
  COALESCE(WHIP, 0) AS WHIP,
  COALESCE(WL, 0) AS WL,
  COALESCE(K, 0) AS K,
  COALESCE(S, 0) AS S,
  Teams.Name AS TeamName
FROM Teams
LEFT OUTER JOIN TeamScoring
 ON TeamScoring.TeamID = Teams.ID;

CREATE OR REPLACE VIEW TeamCatRankings AS
SELECT TeamID,
(SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.OBP > ts.OBP) AS OBPRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.SLG > ts.SLG) AS SLGRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.RHR > ts.RHR) AS RHRRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.RBI > ts.RBI) AS RBIRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.HR > ts.HR) AS HRRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.SBC > ts.SBC) AS SBCRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.INN > ts.INN) AS INNRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND (ts2.ERA < ts.ERA OR (ts2.ERA > 0 AND ts.ERA = 0))) AS ERARank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND (ts2.WHIP < ts.WHIP OR (ts2.WHIP > 0 AND ts.WHIP = 0))) AS WHIPRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.WL > ts.WL) AS WLRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.K > ts.K) AS KRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.S > ts.S) AS SRank
FROM TeamScoringWithZeroes ts;

CREATE OR REPLACE VIEW TeamRankings AS
SELECT t.Name, ts.*,
  (OBPRank + SLGRank + RHRRank + RBIRank + HRRank + SBCRank) AS BattingRank,
  (INNRank + ERARank + WHIPRank + WLRank + KRank + SRank) AS PitchingRank,
  (OBPRank + SLGRank + RHRRank + RBIRank + HRRank + SBCRank + INNRank + ERARank + WHIPRank + WLRank + KRank + SRank) AS TotalRank
FROM TeamCatRankings ts
INNER JOIN Teams t
 on ts.TeamID = t.ID
ORDER BY (OBPRank + SLGRank + RHRRank + RBIRank + HRRank + SBCRank + INNRank + ERARank + WHIPRank + WLRank + KRank + SRank);

CREATE OR REPLACE VIEW DraftResultsDisplay AS
SELECT dr.*, t.DisplayUser, CONCAT_WS(', ', p.LastName, p.FirstName) AS PlayerName
FROM DraftResults dr
INNER JOIN Teams t
 ON dr.TeamID = t.ID
INNER JOIN Players p
 ON dr.PlayerID = p.ID;

CREATE OR REPLACE VIEW DraftResultsLoad AS
  SELECT dr.PlayerID,
    dr.Round,
    dr.Pick,
    dr.Keeper,
    CONCAT_WS(' ', p.FirstName, p.LastName) AS PlayerName,
    COALESCE(p.Eligibility, 'P') AS Eligibility,
    t.DraftOrder
  FROM DraftResults dr
    INNER JOIN Teams t
      ON dr.TeamID = t.ID
    INNER JOIN Players p
      ON dr.PlayerID = p.ID
  WHERE dr.BackedOut = 0;
