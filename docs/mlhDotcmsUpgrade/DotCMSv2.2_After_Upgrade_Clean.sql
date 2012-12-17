
----------------------------------------------------------------------------
--  After database upgrade and before you reindex
----------------------------------------------------------------------------



update identifier set asset_name = (id+'.containers') where
id in(select identifier from contentlet) and asset_type <>
'containers';

update identifier set asset_type = 'containers' where id in(select
identifier from containers) and asset_type <> 'containers';

update identifier set parent_path = '/' where id in(select identifier
from contentlet) and asset_type <> 'contentlet';

update identifier set asset_name = (id+'.content') where id
in(select identifier from contentlet) and asset_type <> 'contentlet'
and asset_name not like '%SYSTEM_HOST%';

update identifier set asset_type = 'contentlet' where id in(select
identifier from contentlet) and asset_type <> 'contentlet';