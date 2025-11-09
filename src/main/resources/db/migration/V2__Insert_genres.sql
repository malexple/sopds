-- Insert common FB2 genres based on FictionBook 2.0 specification
-- These are typical Russian e-book genres

-- Fiction genres
INSERT INTO genres (code, name_ru, name_en, description) VALUES
('sf_history', 'Альтернативная история', 'Alternative History', 'Science fiction with alternative historical scenarios'),
('sf_action', 'Боевая фантастика', 'Action Science Fiction', 'Action-packed science fiction'),
('sf_epic', 'Эпическая фантастика', 'Epic Fantasy', 'Epic fantasy and science fiction'),
('sf_heroic', 'Героическая фантастика', 'Heroic Fantasy', 'Heroic fantasy'),
('sf_detective', 'Детективная фантастика', 'Detective Science Fiction', 'Mystery and detective in SF setting'),
('sf_cyberpunk', 'Киберпанк', 'Cyberpunk', 'Cyberpunk science fiction'),
('sf_space', 'Космическая фантастика', 'Space Opera', 'Space opera and space exploration'),
('sf_social', 'Социальная фантастика', 'Social Science Fiction', 'Social and philosophical SF'),
('sf_horror', 'Ужасы и Мистика', 'Horror', 'Horror and mysticism'),
('sf_fantasy', 'Фэнтези', 'Fantasy', 'Fantasy fiction'),
('sf', 'Научная Фантастика', 'Science Fiction', 'General science fiction');

-- Detective and thriller
INSERT INTO genres (code, name_ru, name_en, description) VALUES
('det_classic', 'Классический детектив', 'Classic Detective', 'Classic detective stories'),
('det_police', 'Полицейский детектив', 'Police Procedural', 'Police procedural'),
('det_action', 'Боевик', 'Action', 'Action and adventure'),
('det_irony', 'Иронический детектив', 'Cozy Mystery', 'Humorous detective'),
('det_history', 'Исторический детектив', 'Historical Detective', 'Historical mystery'),
('det_espionage', 'Шпионский детектив', 'Spy Thriller', 'Espionage and spy fiction'),
('det_crime', 'Криминальный детектив', 'Crime Fiction', 'Crime fiction'),
('det_political', 'Политический детектив', 'Political Thriller', 'Political thriller'),
('det_maniac', 'Маньяк', 'Serial Killer', 'Serial killer fiction'),
('det_hard', 'Крутой детектив', 'Hard-boiled', 'Hard-boiled detective'),
('thriller', 'Триллер', 'Thriller', 'Thriller'),
('detective', 'Детектив', 'Detective', 'General detective fiction');

-- Prose
INSERT INTO genres (code, name_ru, name_en, description) VALUES
('prose_classic', 'Классическая проза', 'Classic Prose', 'Classic literature'),
('prose_history', 'Историческая проза', 'Historical Fiction', 'Historical fiction'),
('prose_contemporary', 'Современная проза', 'Contemporary Fiction', 'Contemporary fiction'),
('prose_counter', 'Контркультура', 'Counterculture', 'Counterculture literature'),
('prose_rus_classic', 'Русская классическая проза', 'Russian Classics', 'Russian classical literature'),
('prose_su_classics', 'Советская классическая проза', 'Soviet Classics', 'Soviet era classics');

-- Romance
INSERT INTO genres (code, name_ru, name_en, description) VALUES
('love_contemporary', 'Современные любовные романы', 'Contemporary Romance', 'Contemporary romance'),
('love_history', 'Исторические любовные романы', 'Historical Romance', 'Historical romance'),
('love_detective', 'Остросюжетные любовные романы', 'Romantic Suspense', 'Romantic suspense'),
('love_short', 'Короткие любовные романы', 'Short Romance', 'Short romance stories'),
('love_erotica', 'Эротика', 'Erotica', 'Erotic fiction');

-- Adventure
INSERT INTO genres (code, name_ru, name_en, description) VALUES
('adv_western', 'Вестерн', 'Western', 'Western fiction'),
('adv_history', 'Исторические приключения', 'Historical Adventure', 'Historical adventure'),
('adv_indian', 'Приключения про индейцев', 'Native American', 'Native American adventures'),
('adv_maritime', 'Морские приключения', 'Maritime Adventure', 'Sea adventures'),
('adv_geo', 'Путешествия и география', 'Travel', 'Travel and geography'),
('adv_animal', 'Природа и животные', 'Nature', 'Nature and animals'),
('adventure', 'Приключения', 'Adventure', 'General adventure');

-- Children's
INSERT INTO genres (code, name_ru, name_en, description) VALUES
('child_tale', 'Сказка', 'Fairy Tale', 'Fairy tales'),
('child_verse', 'Детские стихи', 'Children Verse', 'Children poetry'),
('child_prose', 'Детская проза', 'Children Prose', 'Children prose'),
('child_sf', 'Детская фантастика', 'Children SF', 'Children science fiction'),
('child_det', 'Детские остросюжетные', 'Children Mystery', 'Children mystery'),
('child_adv', 'Детские приключения', 'Children Adventure', 'Children adventure'),
('child_education', 'Детская образовательная', 'Educational', 'Educational for children');

-- Poetry and Drama
INSERT INTO genres (code, name_ru, name_en, description) VALUES
('poetry', 'Поэзия', 'Poetry', 'Poetry'),
('dramaturgy', 'Драматургия', 'Drama', 'Drama and plays');

-- Non-fiction
INSERT INTO genres (code, name_ru, name_en, description) VALUES
('antique_ant', 'Античная литература', 'Ancient Literature', 'Ancient literature'),
('antique', 'Старинное', 'Antique', 'Antique literature'),
('sci_history', 'История', 'History', 'History'),
('sci_psychology', 'Психология', 'Psychology', 'Psychology'),
('sci_culture', 'Культурология', 'Cultural Studies', 'Cultural studies'),
('sci_religion', 'Религиоведение', 'Religious Studies', 'Religious studies'),
('sci_philosophy', 'Философия', 'Philosophy', 'Philosophy'),
('sci_politics', 'Политика', 'Politics', 'Politics'),
('sci_business', 'Деловая литература', 'Business', 'Business literature'),
('sci_juris', 'Юриспруденция', 'Law', 'Law and jurisprudence'),
('sci_linguistic', 'Языкознание', 'Linguistics', 'Linguistics'),
('sci_medicine', 'Медицина', 'Medicine', 'Medicine'),
('sci_phys', 'Физика', 'Physics', 'Physics'),
('sci_math', 'Математика', 'Mathematics', 'Mathematics'),
('sci_chem', 'Химия', 'Chemistry', 'Chemistry'),
('sci_biology', 'Биология', 'Biology', 'Biology'),
('sci_tech', 'Технические науки', 'Technical Sciences', 'Technical sciences'),
('science', 'Научная литература', 'Science', 'General science');

-- Reference
INSERT INTO genres (code, name_ru, name_en, description) VALUES
('comp_www', 'Интернет', 'Internet', 'Internet and web'),
('comp_programming', 'Программирование', 'Programming', 'Programming'),
('comp_hard', 'Компьютерное железо', 'Computer Hardware', 'Computer hardware'),
('comp_soft', 'Программное обеспечение', 'Software', 'Software'),
('comp_db', 'Базы данных', 'Databases', 'Databases'),
('comp_osnet', 'ОС и Сети', 'OS and Networks', 'Operating systems and networks'),
('computers', 'Компьютеры и Интернет', 'Computers', 'General computer topics'),
('reference_encyc', 'Энциклопедии', 'Encyclopedia', 'Encyclopedia'),
('reference_dict', 'Словари', 'Dictionary', 'Dictionaries'),
('reference_ref', 'Справочники', 'Reference', 'Reference books'),
('reference_guide', 'Руководства', 'Guides', 'Guides and manuals'),
('reference', 'Справочная литература', 'Reference', 'General reference');

-- Other
INSERT INTO genres (code, name_ru, name_en, description) VALUES
('nonf_biography', 'Биографии и Мемуары', 'Biography', 'Biography and memoirs'),
('nonf_publicism', 'Публицистика', 'Publicism', 'Publicism'),
('design', 'Искусство и Дизайн', 'Art and Design', 'Art and design'),
('antique_myths', 'Мифы. Легенды. Эпос', 'Myths and Legends', 'Myths, legends and epic'),
('religion', 'Религия', 'Religion', 'Religion'),
('religion_rel', 'Религиозные тексты', 'Religious Texts', 'Religious texts'),
('religion_esoterics', 'Эзотерика', 'Esoterics', 'Esoteric literature'),
('religion_self', 'Самосовершенствование', 'Self-improvement', 'Self-improvement'),
('humor', 'Юмор', 'Humor', 'Humor'),
('home_cooking', 'Кулинария', 'Cooking', 'Cooking'),
('home_pets', 'Домашние животные', 'Pets', 'Pets'),
('home_crafts', 'Хобби и ремесла', 'Crafts', 'Hobbies and crafts'),
('home_entertain', 'Развлечения', 'Entertainment', 'Entertainment'),
('home_health', 'Здоровье', 'Health', 'Health'),
('home_garden', 'Сад и огород', 'Garden', 'Gardening'),
('home_diy', 'Сделай сам', 'DIY', 'Do it yourself'),
('home_sport', 'Спорт', 'Sport', 'Sports'),
('home_sex', 'Эротика и секс', 'Erotica', 'Erotica and sex'),
('home', 'Дом и семья', 'Home', 'Home and family');