CREATE OR REPLACE VIEW BattingWeeks AS (
SELECT MAX(Team) AS BTeam, TeamID AS BTeamID, MAX(`Year`) AS `BYear`, Period AS BPeriod,
SUM(FPTS) AS BFPTS,
SUM(AB) AS AB,
SUM(H) AS H,
SUM(BB) AS BB,
SUM(CS) AS CS,
SUM(HR) AS HR,
SUM(KO) AS KO,
SUM(HP) AS HP,
SUM(R) AS R,
SUM(RBI) AS RBI,
SUM(SB) AS SB,
SUM(TB) AS TB,
SUM(RHR) AS RHR,
SUM(SBC) AS SBC,
SUM(SF) AS SF,
COUNT(*) AS NumBatters
FROM Batting
WHERE Team <> 'FA'
AND Status = 'A'
GROUP BY TeamID, Period
ORDER BY Period, TeamID);

CREATE OR REPLACE VIEW PitchingWeeks AS (
SELECT MAX(Team) AS PTeam, TeamID AS PTeamID, MAX(`Year`) AS `PYear`, Period AS PPeriod,
SUM(FPTS) AS PFPTS,
SUM(INN) AS INN,
SUM(BBI) AS BBI,
SUM(ER) AS ER,
SUM(HA) AS HA,
SUM(K) AS K,
SUM(L) AS L,
SUM(S) AS S,
SUM(W) AS W,
SUM(GS) AS GS,
SUM(BRA) AS BRA,
SUM(WL) AS WL,
(SELECT COUNT(*) FROM Pitching p WHERE p.Role = 'Starter' AND Pitching.TeamID = p.TeamID AND Pitching.Period = p.Period AND p.Status = 'A') AS Starters,
(SELECT COUNT(*) FROM Pitching p WHERE p.Role = 'Reliever' AND Pitching.TeamID = p.TeamID AND Pitching.Period = p.Period AND p.Status = 'A') AS Relievers,
(SELECT COUNT(*) FROM Pitching p WHERE p.GS = 2 AND Pitching.TeamID = p.TeamID AND Pitching.Period = p.Period AND p.Status = 'A') AS DoubleStarts,
(SELECT SUM(INN)/SUM(GS) FROM Pitching p WHERE p.APP = p.GS AND Pitching.TeamID = p.TeamID AND Pitching.Period = p.Period AND p.Status = 'A') AS INNdGS,
COUNT(*) AS NumPitchers
FROM Pitching
WHERE Team <> 'FA'
AND Status = 'A'
GROUP BY TeamID, Period
ORDER BY Period, TeamID);

CREATE OR REPLACE VIEW WeeklyScoring AS (
SELECT BTeamID AS TeamID, BPeriod AS Period, BFPTS + PFPTS AS FPTS, b.*, p.*,
  (H+BB+HP)/(AB+BB+HP+SF) AS OBP, (TB/AB) AS SLG, (H/AB) AS BA,
  (ER/INN)*9 AS ERA, (BBI+HA)/INN AS WHIP
FROM BattingWeeks b
INNER JOIN PitchingWeeks p
 ON b.BTeamID = p.PTeamID AND b.BPeriod = p.PPeriod
);


CREATE OR REPLACE VIEW Matchups AS (
SELECT s.Year, s.Period, s.Team1, s.Team2, w1.TeamID AS Team1ID, w2.TeamID AS Team2ID,
w1.FPTS AS FPTS1, w2.FPTS AS FPTS2,
(CASE WHEN w1.INN > w2.INN THEN 1 WHEN w2.INN > w1.INN THEN 0 ELSE 0.5 END) AS INNCat,
(CASE WHEN w1.ER < w2.ER THEN 1 WHEN w2.ER < w1.ER THEN 0 ELSE 0.5 END) AS ERCat,
(CASE WHEN w1.BRA < w2.BRA THEN 1 WHEN w2.BRA < w1.BRA THEN 0 ELSE 0.5 END) AS BRACat,
(CASE WHEN w1.WL > w2.WL THEN 1 WHEN w2.WL > w1.WL THEN 0 ELSE 0.5 END) AS WLCat,
(CASE WHEN w1.S > w2.S THEN 1 WHEN w2.S > w1.S THEN 0 ELSE 0.5 END) AS SCat,
(CASE WHEN w1.K > w2.K THEN 1 WHEN w2.K > w1.K THEN 0 ELSE 0.5 END) AS KCat,
(CASE WHEN w1.OBP > w2.OBP THEN 1 WHEN w2.OBP > w1.OBP THEN 0 ELSE 0.5 END) AS OBPCat,
(CASE WHEN w1.SLG > w2.SLG THEN 1 WHEN w2.SLG > w1.SLG THEN 0 ELSE 0.5 END) AS SLGCat,
(CASE WHEN w1.RHR > w2.RHR THEN 1 WHEN w2.RHR > w1.RHR THEN 0 ELSE 0.5 END) AS RHRCat,
(CASE WHEN w1.RBI > w2.RBI THEN 1 WHEN w2.RBI > w1.RBI THEN 0 ELSE 0.5 END) AS RBICat,
(CASE WHEN w1.SBC > w2.SBC THEN 1 WHEN w2.SBC > w1.SBC THEN 0 ELSE 0.5 END) AS SBCCat,
(CASE WHEN w1.HR > w2.HR THEN 1 WHEN w2.HR > w1.HR THEN 0 ELSE 0.5 END) AS HRCat,
(CASE WHEN w1.ERA < w2.ERA THEN 1 WHEN w2.ERA < w1.ERA THEN 0 ELSE 0.5 END) AS ERACat,
(CASE WHEN w1.WHIP < w2.WHIP THEN 1 WHEN w2.WHIP < w1.WHIP THEN 0 ELSE 0.5 END) AS WHIPCat,
(CASE WHEN w1.BA > w2.BA THEN 1 WHEN w2.BA > w1.BA THEN 0 ELSE 0.5 END) AS BACat,
(CASE WHEN w1.W > w2.W THEN 1 WHEN w2.W > w1.W THEN 0 ELSE 0.5 END) AS WCat,
(CASE WHEN w1.R > w2.R THEN 1 WHEN w2.R > w1.R THEN 0 ELSE 0.5 END) AS RCat,
(CASE WHEN w1.SB > w2.SB THEN 1 WHEN w2.SB > w1.SB THEN 0 ELSE 0.5 END) AS SBCat,
w1.INN AS INN1,
w1.ER AS ER1,
w1.BRA AS BRA1,
w1.WL AS WL1,
w1.S AS S1,
w1.K AS K1,
w1.OBP AS OBP1,
w1.SLG AS SLG1,
w1.RHR AS RHR1,
w1.RBI AS RBI1,
w1.SBC AS SBC1,
w1.HR AS HR1,
w1.ERA AS ERA1,
w1.DoubleStarts AS DoubleStarts1,
w1.Starters AS Starters1,
w1.Relievers AS Relievers1,
w1.NumPitchers AS NumPitchers1,
w1.INNdGS AS INNdGS1,
w2.INN AS INN2,
w2.ER AS ER2,
w2.BRA AS BRA2,
w2.WL AS WL2,
w2.S AS S2,
w2.K AS K2,
w2.OBP AS OBP2,
w2.SLG AS SLG2,
w2.RHR AS RHR2,
w2.RBI AS RBI2,
w2.SBC AS SBC2,
w2.HR AS HR2,
w2.Starters AS Starters2,
w2.Relievers AS Relievers2,
w2.NumPitchers AS NumPitchers2
FROM Schedule s
INNER JOIN WeeklyScoring w1
  ON s.Team1ID = w1.TeamID AND s.Period = w1.Period
INNER JOIN WeeklyScoring w2
  ON s.Team2ID = w2.TeamID AND s.Period = w2.Period
ORDER BY Year, Period, Team1
);

CREATE OR REPLACE VIEW MatchupsGroupedByBattingPitching AS (
SELECT
(INNCat + ERCat + BRACat + WLCat + SCat + KCat) AS ShangPitching1,
(OBPCat + SLGCat + RHRCat + RBICat + SBCCat + HRCat) AS ShangBatting1,
(BACat + RCat + RBICat + HRCat + SBCat) AS BieberBatting1,
(ERACat + WHIPCat + WCat + SCat + KCat) AS BieberPitching1,
Matchups.*
FROM Matchups
);

CREATE OR REPLACE VIEW MatchupsGroupedByTotals AS (
SELECT
6 - ShangBatting1 AS ShangBatting2,
6 - ShangPitching1 AS ShangPitching2,
(ShangBatting1 + ShangPitching1) AS Shang1,
12 - (ShangBatting1 + ShangPitching1) AS Shang2,

CASE
  WHEN (ShangPitching1 > 3) THEN 1
  WHEN (ShangPitching1 = 3) THEN 0
  ELSE -1
END AS ShangPitchingWin,

5 - BieberBatting1 AS BieberBatting2,
5 - BieberPitching1 AS BieberPitching2,
(BieberBatting1 + BieberPitching1) AS Bieber1,
10 - (BieberBatting1 + BieberPitching1) AS Bieber2,

CASE
  WHEN (BieberPitching1 > 2.5) THEN 1
  WHEN (BieberPitching1 = 2.5) THEN 0
  ELSE -1
END AS BieberPitchingWin,

MatchupsGroupedByBattingPitching.*

FROM MatchupsGroupedByBattingPitching
);

CREATE OR REPLACE VIEW ChewieComparison AS (
SELECT FPTS2-FPTS1 AS Diff1, Shang2-Shang1 AS Diff2, MatchupsGroupedByTotals.*
FROM MatchupsGroupedByTotals
ORDER BY (FPTS2-FPTS1)*(Shang2-Shang1)
);

CREATE OR REPLACE VIEW BieberComparison AS (
SELECT ABS((Bieber2-Bieber1)*(10)-(Shang2-Shang1)*(100/12)) AS ModifiedDiff,
(Bieber2-Bieber1)*10 AS Diff1, (Shang2-Shang1)*(100/12) AS Diff2, MatchupsGroupedByTotals.*
FROM MatchupsGroupedByTotals
ORDER BY ABS((Bieber2-Bieber1)*(10)-(Shang2-Shang1)*(100/12)) DESC
);

CREATE OR REPLACE VIEW BieberPitchComparison AS (
SELECT ABS((BieberPitching2-BieberPitching1)*(10)-(ShangPitching2-ShangPitching1)*(100/12)) AS ModifiedDiff,
(BieberPitching2-BieberPitching1)*10 AS Diff1, (ShangPitching2-ShangPitching1)*(100/12) AS Diff2, MatchupsGroupedByTotals.*
FROM MatchupsGroupedByTotals
ORDER BY ABS((BieberPitching2-BieberPitching1)*(10)-(ShangPitching2-ShangPitching1)*(100/12)) DESC
);

CREATE OR REPLACE VIEW BieberBatComparison AS (
SELECT ABS((BieberBatting2-BieberBatting1)*(10)-(ShangBatting2-ShangBatting1)*(100/12)) AS ModifiedDiff,
(BieberBatting2-BieberBatting1)*10 AS Diff1, (ShangBatting2-ShangBatting1)*(100/12) AS Diff2, MatchupsGroupedByTotals.*
FROM MatchupsGroupedByTotals
ORDER BY ABS((BieberBatting2-BieberBatting1)*(10)-(ShangBatting2-ShangBatting1)*(100/12)) DESC
);

SELECT ShangPitching1, AVG(Relievers1), AVG(Starters1)
FROM BieberPitchComparison
WHERE NumPitchers1 = 7
GROUP BY ShangPitching1
ORDER BY ShangPitching1 DESC;

SELECT BieberPitchingWin, AVG(Relievers1), AVG(Starters1)
FROM BieberPitchComparison
WHERE NumPitchers1 = 7
GROUP BY BieberPitchingWin
ORDER BY BieberPitchingWin DESC;

SELECT Relievers1, AVG(ERA1), AVG(ShangPitching1), AVG(BieberPitching1), AVG(FPTS1), COUNT(*)
FROM BieberPitchComparison
WHERE NumPitchers1 = 7
GROUP BY Relievers1
ORDER BY Relievers1;

SELECT DoubleStarts1, AVG(ERA1), AVG(ShangPitching1), AVG(BieberPitching1), AVG(FPTS1), AVG(FPTS2), COUNT(*)
FROM BieberPitchComparison
WHERE NumPitchers1 = 7
GROUP BY DoubleStarts1
ORDER BY DoubleStarts1;

SELECT FLOOR(INNdGS1), AVG(ERA1), AVG(ShangPitching1), AVG(BieberPitching1), AVG(FPTS1), AVG(FPTS2), COUNT(*)
FROM BieberPitchComparison
WHERE NumPitchers1 = 7
GROUP BY FLOOR(INNdGS1)
ORDER BY FLOOR(INNdGS1);



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
SELECT Year,
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
WHERE Role IN ('Starter', 'Closer')
GROUP BY Year;

CREATE OR REPLACE VIEW StdDeviationBatting AS
SELECT Year,
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
WHERE AB > 400
GROUP BY Year;


CREATE OR REPLACE VIEW AvgPitching AS
SELECT Year,
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
WHERE Role IN ('Starter', 'Closer')
GROUP BY Year;


CREATE OR REPLACE VIEW AvgBatting AS
SELECT Year,
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
WHERE AB > 400
GROUP BY Year;


CREATE OR REPLACE VIEW StdDeviationBattingByPos AS
SELECT yb.Year, Position,
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
GROUP BY yb.Year, Position;

CREATE OR REPLACE VIEW AvgBattingByPos AS
SELECT yb.Year, Position,
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
GROUP BY yb.Year, Position;

CREATE OR REPLACE VIEW StdDeviationPitchingByPos AS
SELECT yb.Year, Role,
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
GROUP BY yb.Year, Role;

CREATE OR REPLACE VIEW AvgPitchingByPos AS
SELECT yb.Year, Role,
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
GROUP BY yb.Year, Role;

CREATE OR REPLACE VIEW AllPlayers AS
SELECT p.*,
      CASE WHEN (dr.BackedOut = 1 OR dr.ID IS NULL) THEN 0 ELSE 1 END AS Drafted,
      CASE WHEN k.ID IS NULL THEN 0 ELSE 1 END AS Keeper
FROM Players p
LEFT OUTER JOIN DraftResults dr
  ON dr.PlayerID = p.ID
LEFT OUTER JOIN Keepers k
  ON k.PlayerID = p.ID;

CREATE OR REPLACE VIEW UnclaimedPlayers AS
SELECT *
FROM AllPlayers
WHERE Drafted = 0 AND Keeper = 0;

CREATE OR REPLACE VIEW UnclaimedPlayersOrdered AS
(SELECT p.ID AS PlayerID, p.PlayerString AS Player, yp.Rank, yp.Year, 'Pitcher' AS PlayerType
FROM UnclaimedPlayers p
INNER JOIN YearlyPitching yp
  ON p.ID = yp.PlayerID
WHERE yp.Year = p.Year)
UNION
(SELECT p.ID AS PlayerID, p.PlayerString AS Player, yb.Rank, yb.Year, 'Batter' AS PlayerType
FROM UnclaimedPlayers p
INNER JOIN YearlyBatting yb
  ON p.ID = yb.PlayerID
WHERE yb.Year = p.Year)
ORDER BY Year DESC,Rank;

CREATE OR REPLACE VIEW UnclaimedBatters AS
SELECT yb.*, FirstName, LastName, MLBTeam
FROM UnclaimedPlayers p
INNER JOIN YearlyBatScoring yb
 ON (yb.PlayerID = p.ID AND p.Year = yb.Year)
WHERE AB > 300;

CREATE OR REPLACE VIEW UnclaimedBattersByPos AS
SELECT e.Position, yb.*
FROM UnclaimedPlayers p
INNER JOIN YearlyBatScoring yb
 ON (yb.PlayerID = p.ID AND p.Year = yb.Year)
INNER JOIN Eligibilities e
 ON p.ID = e.PlayerID
WHERE AB > 300;

CREATE OR REPLACE VIEW UnclaimedPitchers AS
SELECT yp.*, FirstName, LastName, MLBTeam
FROM UnclaimedPlayers p
INNER JOIN YearlyPitchScoring yp
 ON (yp.PlayerID = p.ID AND yp.Year = p.Year)
WHERE OUTS > 120;

CREATE OR REPLACE VIEW TeamBatting AS
SELECT TeamID, MIN(TeamName), yb.Year,
  SUM(RHR) AS RHR,
  SUM(RBI) AS RBI,
  SUM(SBC) AS SBC,
  SUM(HR) AS HR,
  SUM(TB)/SUM(AB) AS SLG,
  (SUM(H)+SUM(BB)+IFNULL(SUM(HP),0))/(SUM(AB)+IFNULL(SUM(HP),0)+IFNULL(SUM(SF),0)) AS OBP,
  COUNT(1) AS NumPlayers
FROM DraftResults dr
INNER JOIN YearlyBatScoring yb
 ON (dr.PlayerID = yb.PlayerID AND yb.Year = dr.Year)
WHERE dr.BackedOut = 0
AND DraftPos <> 'RS'
GROUP BY yb.Year, dr.TeamID
ORDER BY yb.Year, dr.TeamID;

CREATE OR REPLACE VIEW TeamPitching AS
SELECT TeamID, MIN(TeamName), yp.Year,
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
 ON (dr.PlayerID = yp.PlayerID AND dr.Year = yp.Year)
WHERE dr.BackedOut = 0
AND DraftPos <> 'RS'
GROUP BY dr.Year, dr.TeamID
ORDER BY dr.Year, dr.TeamID;

CREATE OR REPLACE VIEW TeamScoring AS
(SELECT tb.TeamID AS TeamID, tb.Year,
  OBP, SLG, RHR, RBI, HR, SBC, INN, ERA, WHIP, WL, K, S
FROM TeamBatting tb
LEFT OUTER JOIN TeamPitching tp
 ON tb.TeamID = tp.TeamID AND tb.Year = tp.Year)
 UNION
 (SELECT tp.TeamID AS TeamID, tp.Year,
  OBP, SLG, RHR, RBI, HR, SBC, INN, ERA, WHIP, WL, K, S
FROM TeamBatting tb
RIGHT OUTER JOIN TeamPitching tp
 ON tb.TeamID = tp.TeamID AND tb.Year = tp.Year);

CREATE OR REPLACE VIEW BatterQualityByCat AS
SELECT yb.*, FirstName, LastName, MLBTeam, Drafted, Keeper, e.Position,
  (yb.TeamOBP-ab.TeamOBP)/std.TeamOBP AS OBPRating,
  (yb.TeamSLG-ab.TeamSLG)/std.TeamSLG AS SLGRating,
  (yb.RHR-ab.RHR)/std.RHR AS RHRRating,
  (yb.RBI-ab.RBI)/std.RBI AS RBIRating,
  (yb.HR-ab.HR)/std.HR AS HRRating,
  (yb.SBC-ab.SBC)/std.SBC AS SBCRating
FROM AvgBattingByPos AS ab
INNER JOIN StdDeviationBattingByPos AS std
 ON ab.Year = std.Year AND ab.Position = std.Position
INNER JOIN Eligibilities e
 ON e.Year = ab.Year AND ab.Position = e.Position
INNER JOIN YearlyBatScoring AS yb
 ON yb.Year = ab.Year AND yb.PlayerID = e.PlayerID
INNER JOIN AllPlayers p
 ON yb.PlayerID = p.ID AND p.Year = yb.Year
WHERE AB > 300
ORDER BY Player, e.Position ASC;

CREATE OR REPLACE VIEW BatterQuality AS
SELECT *, (OBPRating + SLGRating + RHRRating + RBIRating + HRRating + SBCRating) AS Total
FROM BatterQualityByCat
ORDER BY (OBPRating + SLGRating + RHRRating + RBIRating + HRRating + SBCRating) DESC;

CREATE OR REPLACE VIEW GroupedBatterQuality AS
SELECT *
FROM BatterQuality
WHERE NOT EXISTS (SELECT 1 a
                  FROM BatterQuality b2
                  WHERE b2.Total > BatterQuality.Total
                  AND b2.PlayerID = BatterQuality.PlayerID);
                  
CREATE OR REPLACE VIEW PitcherQualityByCat AS
SELECT yp.*, FirstName, LastName, MLBTeam, Drafted, Keeper,
  (yp.INN-ap.INN)/std.INN AS INNRating,
  (ap.TeamERA-yp.TeamERA)/std.TeamERA AS ERARating,
  (ap.TeamWHIP-yp.TeamWHIP)/std.TeamWHIP AS WHIPRating,
  (yp.WL-ap.WL)/std.WL AS WLRating,
  (yp.K-ap.K)/std.K AS KRating,
  (yp.S-ap.S)/std.S AS SRating
FROM AvgPitching AS ap
INNER JOIN StdDeviationPitching AS std
 ON ap.Year = std.Year
INNER JOIN YearlyPitchScoring yp
 ON yp.Year = ap.Year
INNER JOIN AllPlayers AS p
 ON yp.PlayerID = p.ID AND yp.Year = p.Year
WHERE yp.OUTS > 120
ORDER BY Player ASC;

CREATE OR REPLACE VIEW PitcherQuality AS
SELECT *, (INNRating + ERARating + WHIPRating + WLRating + KRating + SRating) AS Total
FROM PitcherQualityByCat
ORDER BY (INNRating + ERARating + WHIPRating + WLRating + KRating + SRating) DESC;

SELECT Player, Role, INN, ER, BRA, WL, K, S,
    FORMAT(ERA, 2) AS ERA,
    FORMAT(WHIP, 3) AS WHIP,
    INNRating, ERARating, WHIPRating , WLRating , KRating , SRating,
    (INNRating + ERARating + WHIPRating + WLRating + KRating + SRating) AS Total
FROM PitcherQualityByCat
WHERE Year = 2012
ORDER BY (INNRating + ERARating + WHIPRating + WLRating + KRating + SRating) DESC;

CREATE OR REPLACE VIEW AllPlayersByQuality AS
(SELECT Player, PlayerID, FirstName, LastName, MLBTeam, Rank, Total, 'P' AS Position, Year
FROM PitcherQuality)
UNION
(SELECT Player, PlayerID, FirstName, LastName, MLBTeam, Rank, Total, Position, Year
FROM BatterQuality)
ORDER BY Total DESC;

CREATE OR REPLACE VIEW UnclaimedDisplayPlayersByQuality AS
(SELECT Player, PlayerID, Rank, Total, 'P' AS Position, Year
FROM PitcherQuality
WHERE Drafted = 0 AND Keeper = 0)
UNION
(SELECT Player, PlayerID, Rank, Total, Position, Year
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
  INN, ERA, WHIP, WL, K, S, Rank, Total, 'P' AS Position, Year,
  FirstName, LastName, MLBTeam
FROM PitcherQuality
WHERE Drafted = 0 AND Keeper = 0)
UNION
(SELECT Player, PlayerID, Eligibility, 'Batter' AS Role,
  OBP, SLG, RHR, RBI, HR, SBC,
  NULL AS INN,
  NULL AS ERA,
  NULL AS WHIP,
  NULL AS WL,
  NULL AS K,
  NULL AS S,
  Rank, Total, Position, Year,
  FirstName, LastName, MLBTeam
FROM BatterQuality
WHERE Drafted = 0 AND Keeper = 0)
ORDER BY Total DESC;

CREATE OR REPLACE VIEW TeamScoringWithZeroes AS
SELECT Teams.ID AS TeamID, Teams.Year,
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
SELECT TeamID, Year,
(SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND ts2.OBP > ts.OBP) AS OBPRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND ts2.SLG > ts.SLG) AS SLGRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND ts2.RHR > ts.RHR) AS RHRRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND ts2.RBI > ts.RBI) AS RBIRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND ts2.HR > ts.HR) AS HRRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND ts2.SBC > ts.SBC) AS SBCRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND ts2.INN > ts.INN) AS INNRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND (ts2.ERA < ts.ERA OR (ts2.ERA > 0 AND ts.ERA = 0))) AS ERARank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND (ts2.WHIP < ts.WHIP OR (ts2.WHIP > 0 AND ts.WHIP = 0))) AS WHIPRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND ts2.WL > ts.WL) AS WLRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND ts2.K > ts.K) AS KRank,
 (SELECT COUNT(1) + 1
 FROM TeamScoringWithZeroes ts2
 WHERE ts2.TeamID <> ts.TeamID
 AND ts2.Year = ts.Year
 AND ts2.S > ts.S) AS SRank
FROM TeamScoringWithZeroes ts
ORDER BY Year DESC;

CREATE OR REPLACE VIEW TeamRankings AS
SELECT t.Name, ts.*,
  (OBPRank + SLGRank + RHRRank + RBIRank + HRRank + SBCRank) AS BattingRank,
  (INNRank + ERARank + WHIPRank + WLRank + KRank + SRank) AS PitchingRank,
  (OBPRank + SLGRank + RHRRank + RBIRank + HRRank + SBCRank + INNRank + ERARank + WHIPRank + WLRank + KRank + SRank) AS TotalRank
FROM TeamCatRankings ts
INNER JOIN Teams t
 on ts.TeamID = t.ID
ORDER BY Year DESC, (OBPRank + SLGRank + RHRRank + RBIRank + HRRank + SBCRank + INNRank + ERARank + WHIPRank + WLRank + KRank + SRank);

CREATE OR REPLACE VIEW DraftResultsDisplay AS
SELECT dr.*, t.DisplayUser, CONCAT_WS(', ', p.LastName, p.FirstName) AS PlayerName
FROM DraftResults dr
INNER JOIN Teams t
 ON dr.TeamID = t.ID
INNER JOIN Players p
 ON dr.PlayerID = p.ID;
