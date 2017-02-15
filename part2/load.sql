LOAD DATA LOCAL INFILE 'locationData.csv' INTO TABLE location FIELDS TERMINATED BY '|*|' LINES TERMINATED BY '\n';
LOAD DATA LOCAL INFILE 'sellerData.csv' INTO TABLE seller FIELDS TERMINATED BY '|*|' LINES TERMINATED BY '\n';
LOAD DATA LOCAL INFILE 'bidderData.csv' INTO TABLE bidder FIELDS TERMINATED BY '|*|' LINES TERMINATED BY '\n';
LOAD DATA LOCAL INFILE 'itemData.csv' INTO TABLE item FIELDS TERMINATED BY '|*|' LINES TERMINATED BY '\n';
LOAD DATA LOCAL INFILE 'categoryData.csv' INTO TABLE category FIELDS TERMINATED BY '|*|' LINES TERMINATED BY '\n';
LOAD DATA LOCAL INFILE 'bidData.csv' INTO TABLE bid FIELDS TERMINATED BY '|*|' LINES TERMINATED BY '\n';
LOAD DATA LOCAL INFILE 'categoryListData.csv' INTO TABLE categoryList FIELDS TERMINATED BY '|*|' LINES TERMINATED BY '\n';
LOAD DATA LOCAL INFILE 'geoLocationData.csv' INTO TABLE geoLocation FIELDS TERMINATED BY '|*|' LINES TERMINATED BY '\n' (@col1, @col2, @col3) SET itemID = @col1, coords = PointFromText(CONCAT('POINT (', @col2, ' ', @col3, ')'));
