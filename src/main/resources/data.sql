insert into `users` (
                     `username`,
                     `password`,
                     `nick_name`,
                     `email`,
                     `birth`,
                     `school`,
                     `grade`,
                     `address`,
                     `memo`,
                     `country`,
                     `terms_of_use_check`,
                     `privacy_terms_check`,
                     `marketing_terms_check`,
                     `user_status`,
                     `authority`,
                     `login_type`,
                     `subscription_id`,
                     `subscription_price`,
                     `remain_question_count`,
                     `remain_tutoring_count`,
                     `expiration_date`,
                     `subscription_date`,
                     `subscription_day`

)

values ('admin','{noop}1111!','admin','admin@email.com','00000000','school','grade',
        'address','memo','country',true,true,true,'ACTIVE','ADMIN','BASIC',0,0,0,0,'2025-07-10T17:21:12.175438100',
        '2025-07-10T17:21:12.175438100',0
       )
;

insert into `user_roles`(`user_user_id`, `roles`)
values (1, 'USER'),
       (1, 'ADMIN')
;

insert into `video_lessons_category`(`video_category_status`, `category_name`, `parents_category_id`)

values
    ('FIRST_CATEGORY', 'Group1', 0),
    ('FIRST_CATEGORY', 'Group2', 0),
    ('FIRST_CATEGORY', 'Group3', 0),
    ('FIRST_CATEGORY', 'Group4', 0),
    ('FIRST_CATEGORY', 'Group5', 0),
    ('FIRST_CATEGORY', 'Group6', 0),
    ('SECOND_CATEGORY', 'English Literature', 0),
    ('SECOND_CATEGORY', 'English Language', 0),
    ('SECOND_CATEGORY', 'Korean', 0),
    ('SECOND_CATEGORY', 'English B', 0),
    ('SECOND_CATEGORY', 'Mandarin B', 0),
    ('SECOND_CATEGORY', 'Spanish B', 0),
    ('SECOND_CATEGORY', 'Economics', 0),
    ('SECOND_CATEGORY', 'Business & Management', 0),
    ('SECOND_CATEGORY', 'Psychology', 0),
    ('SECOND_CATEGORY', 'Geography', 0),
    ('SECOND_CATEGORY', 'History', 0),
    ('SECOND_CATEGORY', 'Physics', 0),
    ('SECOND_CATEGORY', 'Chemistry', 0),
    ('SECOND_CATEGORY', 'Biology', 0),
    ('SECOND_CATEGORY', 'Design Technology', 0),
    ('SECOND_CATEGORY', 'Math AA', 0),
    ('SECOND_CATEGORY', 'Math AI', 0),
    ('SECOND_CATEGORY', 'Visual Arts', 0),
    ('THIRD_CATEGORY', 'SL', 0),
    ('THIRD_CATEGORY', 'HL', 0),
    ('THIRD_CATEGORY', 'ALL', 0)

;

insert into `subscribe`(`title`, `content`, `price`, `question_count`, `tutoring_count`, `subscribe_month_period`, `video_lessons_count`, `color`, `font_color`)

values
    ('BASIC','구독 내용(HTML 형식 저장가능)',450000, 5,6,1,1,'#fff','#000'),
    ('BASIC','구독 내용(HTML 형식 저장가능)',900000, 5,6,3,1,'#fff','#000'),
    ('BASIC','구독 내용(HTML 형식 저장가능)',1800000, 5,6,6,1,'#fff','#000'),
    ('BASIC','구독 내용(HTML 형식 저장가능)',3600000, 5,6,12,1,'#fff','#000'),
    ('SUPER','구독 내용(HTML 형식 저장가능)',800000, 10,12,1,0,'#51346c','#fff'),
    ('SUPER','구독 내용(HTML 형식 저장가능)',1500000, 10,12,3,0,'#51346c','#fff'),
    ('SUPER','구독 내용(HTML 형식 저장가능)',3000000, 10,12,6,0,'#51346c','#fff'),
    ('SUPER','구독 내용(HTML 형식 저장가능)',6000000, 10,12,12,0,'#51346c','#fff')

;