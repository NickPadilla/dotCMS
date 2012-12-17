delete from permission_reference where len(asset_id)>36;
GO

update identifier set uri='' where inode='SYSTEM_HOST';
Go


update inode set type='contentlet' where exists (select * from contentlet where contentlet.inode=inode.inode);
GO
update inode set type='template' where exists (select * from template where template.inode=inode.inode);
GO
update inode set type='file_asset' where exists (select * from file_asset where file_asset.inode=inode.inode);
GO
update inode set type='links' where exists (select * from links where links.inode=inode.inode);
GO
update inode set type='containers' where exists (select * from containers where containers.inode=inode.inode);
GO
update inode set type='htmlpage' where exists (select * from htmlpage where htmlpage.inode=inode.inode);
GO




-- Tree clean up (each one can take several hours)
DELETE from tree where (parent in(select identifier from inode where type='file_asset') or parent in(select inode from folder)) and child in(select inode from inode where type ='file_asset');
GO
DELETE from tree where parent in(select identifier from inode where type='template')and child in(select inode from inode where type ='template');
GO
DELETE from tree where parent in(select identifier from inode where type='containers')and child in(select inode from inode where type ='containers');
GO
DELETE from tree where parent in(select identifier from inode where type='contentlet')and child in(select inode from inode where type ='contentlet');
GO
DELETE from tree where (parent in(select identifier from inode where type='htmlpage')or parent in(select inode from folder)) and child in(select inode from inode where type ='htmlpage');
GO
DELETE from tree where (parent in(select identifier from inode where type='links') or parent in(select inode from folder)) and child in(select inode from inode where type ='links');
GO
Delete from tree where parent in(select distinct host_inode from identifier) and child in(select inode from inode where type ='containers');
GO
Delete from tree where parent in(select distinct host_inode from identifier) and child in(select inode from inode where type ='template');
GO
Delete from tree where parent in(select distinct host_inode from identifier) and child in(select inode from inode where type ='folder');
GO
Delete from tree where parent in(select inode from inode where type='folder') and child in(select inode from inode where type='folder');
GO
Delete from tree where child in(select inode from inode where type='containers') and parent in(select inode from structure);
GO
Delete from tree where child in(select inode from inode where type='htmlpage') and parent in(select inode from template);
GO

-- Orphan inodes clean up
create table inodeskill (inode varchar(36) primary key);
GO
insert into inodeskill (inode)
(select inode from inode where type='htmlpage' and inode not in (select inode from htmlpage));
GO
insert into inodeskill (inode)
(select inode from inode where type='containers' and inode not in (select inode from containers));
GO
insert into inodeskill (inode)
(select inode from inode where type='template' and inode not in (select inode from template));
GO
insert into inodeskill (inode)
(select inode from inode where type='links' and inode not in (select inode from links));
GO
insert into inodeskill (inode)
(select inode from inode where type='contentlet' and inode not in (select inode from contentlet));
GO
insert into inodeskill (inode)
(select inode from inode where type='file_asset' and inode not in (select inode from file_asset));
GO
delete from permission_reference where asset_id in (select inode from inodeskill);
GO
delete from permission_reference where reference_id in (select inode from inodeskill);
GO
delete from permission where inode_id in (select inode from inodeskill);
GO
delete from tree where parent in (select inode from inodeskill);
GO
delete from tree where child in (select inode from inodeskill);
GO
delete from inode where inode in (select inode from inodeskill);
GO
drop table inodeskill;
GO




DECLARE @ObjectName sysname
DECLARE @StatsName sysname
DECLARE StatsCursor CURSOR FAST_FORWARD
FOR
SELECT OBJECT_NAME(object_id) as 'ObjectName', [name] as 'StatsName'
FROM sys.stats
WHERE (INDEXPROPERTY(object_id, [name], 'IsAutoStatistics') = 1 OR
INDEXPROPERTY(object_id, [name], 'IsStatistics') = 1)
AND OBJECTPROPERTY(object_id, 'IsMSShipped') = 0
OPEN StatsCursor
FETCH NEXT FROM StatsCursor
INTO @ObjectName, @StatsName
WHILE @@FETCH_STATUS = 0
BEGIN
EXEC ('DROP STATISTICS ' + @ObjectName + '.' + @StatsName)
FETCH NEXT FROM StatsCursor
INTO @ObjectName, @StatsName
END
CLOSE StatsCursor
DEALLOCATE StatsCursor;
GO

-------------------------------------------------------------
--need to have this function - it isn't in the database???
-------------------------------------------------------------

CREATE FUNCTION dotFolderPath(@parent_path varchar(36), @asset_name varchar(36)) 
RETURNS varchar(36) 
BEGIN 
  IF(@parent_path='/System folder') 
  BEGIN 
    RETURN '/' 
  END 
  RETURN @parent_path+@asset_name+'/' 
END
GO

-- also not in the database but needs to be
create index tag_user_id_index on tag (user_id)
GO


