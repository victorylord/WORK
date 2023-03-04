UPDATE rcs_tournament_template_play_margain margain
SET margain.is_sell = 1,
    update_time     = NOW()
WHERE margain.id IN (
    SELECT a.id
    FROM (
             SELECT m.id
             FROM rcs_tournament_template t,
                  rcs_tournament_template_play_margain m
             WHERE t.id = m.template_id
               AND t.sport_id = N
               AND t.type = N
               AND t.type_val = N
               AND t.match_type = N
               AND m.play_id IN
                   (
                       1
                       )
         ) a
)