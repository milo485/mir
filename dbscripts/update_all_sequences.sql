-- This script updates all sequences in a postgres mir database

select setval('media_id_seq',         (select max(id) from media));
select setval('media_folder_id_seq',  (select max(id) from media_folder));
select setval('feature_id_seq',       (select max(id) from feature));
select setval('topic_id_seq',         (select max(id) from topic));
select setval('webdb_users_id_seq',   (select max(id) from webdb_users));
select setval('comment_id_seq',       (select max(id) from comment));
select setval('breaking_id_seq',      (select max(id) from breaking));
select setval('messages_id_seq',      (select max(id) from messages));
select setval('media_type_id_seq',    (select max(id) from media_type));

select setval('comment_status_id_seq',(select max(id) from comment_status));
select setval('article_type_id_seq',  (select max(id) from article_type));
select setval('language_id_seq',      (select max(id) from language));




