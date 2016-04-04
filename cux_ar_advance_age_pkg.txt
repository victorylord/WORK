CREATE OR REPLACE PACKAGE cux_ar_advance_age_pkg IS

  /*==================================================
  Copyright (C) Hand Enterprise Solutions Co.,Ltd.
             AllRights Reserved
  ==================================================*/
  /*==================================================
  Program Name:
      CUX_AR_AGXAG_PKG
  Description:
      This program provide concurrent main procedure to perform:
           
  History: 
      1.00  2016-03-29  Frank  Creation
  ==================================================*/

  /*==================================================
  Procedure Name :
      main
  Description:
      This procedure is concurrent entry, perform:
          
  Argument:
      errbuf          : concurrent return message buffer
      retcode         : concurrent return status code
                          0 success / 1 warning / 2 error
      p_parameter1    : 
  History: 
       1.00  2016-03-29  Frank  Creation
  ==================================================*/
  PROCEDURE main(errbuf        OUT VARCHAR2,
                 retcode       OUT VARCHAR2,
                 p_org_id      IN NUMBER,
                 p_customer_id IN NUMBER,
                 p_due_date    IN VARCHAR2);

END cux_ar_advance_age_pkg;
/
CREATE OR REPLACE PACKAGE BODY cux_ar_advance_age_pkg IS

  /*==================================================
  Copyright (C) Hand Enterprise Solutions Co.,Ltd.
             AllRights Reserved
  ==================================================*/
  /*==================================================
  Program Name:
      CUX_AR_AGXAG_PKG
  Description:
      This program provide concurrent main procedure to perform:
           
  History: 
      1.00  2016-03-29  Frank  Creation
  ==================================================*/

  -- Global variable
  g_pkg_name CONSTANT VARCHAR2(30) := 'HAND_CONC_TEMPLATE_PRG';
  -- Debug Enabled
  l_debug VARCHAR2(1) := nvl(fnd_profile.value('AFLOG_ENABLED'),
                             'N');

  PROCEDURE output(p_msg VARCHAR2) IS
  BEGIN
    fnd_file.put_line(fnd_file.output,
                      p_msg);
  END output;

  PROCEDURE log(p_msg VARCHAR2) IS
  BEGIN
    fnd_file.put_line(fnd_file.log,
                      p_msg);
  END log;

  PROCEDURE output_xml(p_tag   IN VARCHAR2,
                       p_value IN VARCHAR2) IS
  BEGIN
    output('<' || upper(p_tag) || '>' || REPLACE(REPLACE(REPLACE(REPLACE(p_value,
                                                                         '&',
                                                                         '&AMP;'),
                                                                 '>',
                                                                 '&GT;'),
                                                         '<',
                                                         '&LT;'),
                                                 '''',
                                                 '&APOS;') || '</' || upper(p_tag) || '>');
  
    fnd_file.put_line(fnd_file.log,
                      '<' || upper(TRIM(p_tag)) || '>' || p_value || '</' || upper(TRIM(p_tag)) || '>');
  END output_xml;

  PROCEDURE output_header(p_tag IN VARCHAR2) IS
  BEGIN
    output('<' || upper(p_tag) || '>');
  END output_header;

  PROCEDURE output_tail(p_tag IN VARCHAR2) IS
  BEGIN
    output('</' || upper(p_tag) || '>');
  END output_tail;

  PROCEDURE process_request(p_init_msg_list IN VARCHAR2 DEFAULT fnd_api.g_false,
                            p_commit        IN VARCHAR2 DEFAULT fnd_api.g_false,
                            x_return_status OUT NOCOPY VARCHAR2,
                            x_msg_count     OUT NOCOPY NUMBER,
                            x_msg_data      OUT NOCOPY VARCHAR2,
                            p_org_id        IN NUMBER,
                            p_customer_id   IN NUMBER,
                            p_due_date      IN VARCHAR2) IS
    l_api_name       CONSTANT VARCHAR2(30) := 'process_request';
    l_savepoint_name CONSTANT VARCHAR2(30) := 'sp_process_request01';
    l_due_date       CONSTANT DATE := fnd_conc_date.string_to_date(p_due_date);
    CURSOR cur_age(p_customer_area IN VARCHAR2) IS
      SELECT t.customer_area,
             t.customer_name,
             SUM(t.remain_amt) remain_amt,
             SUM(t.amt_30y) amt_30d,
             SUM(t.amt_20_30d) amt_20_30d,
             SUM(t.amt_10_20d) amt_10_20d,
             SUM(t.amt_3_10d) amt_3_10d,
             SUM(t.amt_3d) amt_3d
        FROM (SELECT amt.customer_area,
                     amt.customer_name,
                     amt.gl_date,
                     amt.remain_amt,
                     --判断日期
                     (CASE
                       WHEN amt.age_day > 30 THEN
                        amt.remain_amt
                     END) amt_30y,
                     (CASE
                       WHEN amt.age_day > 20
                            AND amt.age_day <= 30 THEN
                        amt.remain_amt
                     END) amt_20_30d,
                     (CASE
                       WHEN amt.age_day > 10
                            AND amt.age_day <= 20 THEN
                        amt.remain_amt
                     END) amt_10_20d,
                     (CASE
                       WHEN amt.age_day > 3
                            AND amt.age_day <= 10 THEN
                        amt.remain_amt
                     END) amt_3_10d,
                     (CASE
                       WHEN amt.age_day <= 3 THEN
                        amt.remain_amt
                     END) amt_3d
                FROM (SELECT ac.customer_name,
                             ac.customer_area,
                             crh_first_posted.gl_date,
                             l_due_date - crh_first_posted.gl_date age_day,
                             (nvl(cr.amount * nvl(cr.exchange_rate,
                                                  1),
                                  0) - (SELECT nvl(SUM(decode(acra.currency_code,
                                                               'CNY',
                                                               nvl(araa.amount_applied * nvl(araa.trans_to_receipt_rate,
                                                                                             1),
                                                                   0),
                                                               nvl(araa.amount_applied,
                                                                   0) * acra.exchange_rate * nvl(araa.trans_to_receipt_rate,
                                                                                                 1))),
                                                    0) amount
                                         
                                           FROM ar_cash_receipts_all           acra,
                                                ar_cash_receipt_history_all    acrha,
                                                ar_receivable_applications_all araa
                                          WHERE 1 = 1
                                            AND acrha.gl_date <= l_due_date
                                               /*    AND (((acra.receipt_method_id = 1042 AND
                                               acrha.status NOT IN ('REMITTED',
                                                                       'CLEARED',
                                                                       'RISK_ELIMINATED') AND nvl(acrha.current_record_flag,
                                                                                                     'Y') = 'Y')) OR
                                               (acra.receipt_method_id <> 1042 AND nvl(acrha.current_record_flag,
                                                                                        'N') = 'Y'))*/
                                            AND EXISTS (SELECT 'A'
                                                   FROM ar_cash_receipt_history_all t
                                                  WHERE t.cash_receipt_id = acrha.cash_receipt_id
                                                    AND t.current_record_flag = 'Y'
                                                    AND t.status != 'REVERSED')
                                            AND acra.cash_receipt_id = acrha.cash_receipt_id
                                            AND upper(acrha.status) != 'REVERSED'
                                            AND araa.cash_receipt_id = acra.cash_receipt_id
                                            AND araa.display = 'Y'
                                            AND araa.gl_date <= l_due_date
                                               --AND araa.applied_customer_trx_id <> -1
                                            AND acra.cash_receipt_id = cr.cash_receipt_id)) remain_amt
                        FROM (SELECT hca.cust_account_id customer_id,
                                     hp.party_name customer_name,
                                     hcas.cust_acct_site_id,
                                     hcsu.site_use_id,
                                     nvl(hcas.attribute1,
                                         '外部客户') customer_area
                                FROM hz_parties             hp,
                                     hz_cust_accounts       hca,
                                     hz_cust_acct_sites_all hcas,
                                     hz_cust_site_uses_all  hcsu
                               WHERE hp.party_id = hca.party_id
                                 AND hca.cust_account_id = hcas.cust_account_id
                                 AND hcas.cust_acct_site_id = hcsu.cust_acct_site_id) ac,
                             ar_cash_receipts_all cr,
                             ar_cash_receipt_history_all crh_first_posted
                       WHERE 1 = 1
                         AND cr.pay_from_customer = ac.customer_id
                         AND cr.customer_site_use_id = ac.site_use_id
                         AND crh_first_posted.cash_receipt_id = cr.cash_receipt_id
                         AND crh_first_posted.org_id = cr.org_id
                         AND crh_first_posted.first_posted_record_flag = 'Y'
                            -- AND cr.receipt_number = 'FR01'                     
                         AND NOT EXISTS (SELECT 1
                                FROM ar_cash_receipt_history_all crh_current --冲销日期; 
                               WHERE crh_current.cash_receipt_id = cr.cash_receipt_id
                                 AND crh_current.org_id = cr.org_id
                                 AND crh_current.current_record_flag = nvl('Y',
                                                                           cr.receipt_number)
                                 AND crh_current.status = 'REVERSED')
                            --paramaters
                         AND mo_global.check_access(cr.org_id) = 'Y'
                         AND crh_first_posted.gl_date <= l_due_date
                         AND (cr.org_id = p_org_id OR p_org_id IS NULL)
                         AND (cr.pay_from_customer = p_customer_id OR p_customer_id IS NULL)
                      --paramaters             
                       ORDER BY customer_area,
                                customer_name) amt
               WHERE amt.remain_amt != 0) t
       WHERE 1 = 1
         AND t.customer_area = p_customer_area
       GROUP BY t.customer_area,
                t.customer_name;
  
    l_org_name   VARCHAR2(240);
    l_year       NUMBER;
    l_month      NUMBER;
    l_day        NUMBER;
    l_print_date VARCHAR2(240);
    l_para_date  VARCHAR2(240);
  BEGIN
    -- start activity to create savepoint, check compatibility
    -- and initialize message list, include debug message hint to enter api
    x_return_status := cux_api.start_activity(p_pkg_name       => g_pkg_name,
                                              p_api_name       => l_api_name,
                                              p_savepoint_name => l_savepoint_name,
                                              p_init_msg_list  => p_init_msg_list);
    IF (x_return_status = fnd_api.g_ret_sts_unexp_error) THEN
      RAISE fnd_api.g_exc_unexpected_error;
    ELSIF (x_return_status = fnd_api.g_ret_sts_error) THEN
      RAISE fnd_api.g_exc_error;
    END IF;
    -- API body
  
    -- logging parameters
  
    log('p_org_id : ' || p_org_id);
    log('p_customer_id : ' || p_customer_id);
    log('p_due_date : ' || p_due_date);
  
    -- todo
    BEGIN
      SELECT hou.name
        INTO l_org_name
        FROM hr_operating_units hou
       WHERE hou.organization_id = p_org_id;
    EXCEPTION
      WHEN no_data_found THEN
        log('当前OU未设置');
    END;
    l_year       := substr(p_due_date,
                           1,
                           4);
    l_month      := substr(p_due_date,
                           6,
                           2);
    l_day        := substr(p_due_date,
                           9,
                           2);
    l_print_date := to_char(SYSDATE,
                            'YYYY-MM-DD');
    output('<?xml version="1.0" encoding="UTF-8"?>');
  
    output_header('HEADER');
    output_xml('ORG_NAME',
               l_org_name);
    output_xml('YEAR',
               l_year);
    output_xml('MONTH',
               l_month);
    output_xml('DAY',
               l_day);
    output_xml('SYSDATE',
               l_print_date);
  
    FOR rec_age IN cur_age('内部单位')
    LOOP
      output_header('INNER_LINES');
    
      output_xml('CUSTOMER_AREA',
                 rec_age.customer_area);
      output_xml('CUSTOMER_NAME',
                 rec_age.customer_name);
      output_xml('REMAIN_AMT',
                 rec_age.remain_amt);
      output_xml('AMT_30D',
                 rec_age.amt_30d);
      output_xml('AMT_30D_PERCENT',
                 CASE WHEN rec_age.amt_30d IS NULL THEN NULL ELSE round(rec_age.amt_30d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_20_30D',
                 rec_age.amt_20_30d);
    
      output_xml('AMT_20_30D_PERCENT',
                 CASE WHEN rec_age.amt_20_30d IS NULL THEN NULL ELSE round(rec_age.amt_20_30d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_10_20D',
                 rec_age.amt_10_20d);
      output_xml('AMT_10_20D_PERCENT',
                 CASE WHEN rec_age.amt_10_20d IS NULL THEN NULL ELSE round(rec_age.amt_10_20d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_3_10D',
                 rec_age.amt_3_10d);
      output_xml('AMT_3_10D_PERCENT',
                 CASE WHEN rec_age.amt_3_10d IS NULL THEN NULL ELSE round(rec_age.amt_3_10d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_3D',
                 rec_age.amt_3d);
      output_xml('AMT_3D_PERCENT',
                 CASE WHEN rec_age.amt_3d IS NULL THEN NULL ELSE round(rec_age.amt_3d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_tail('INNER_LINES');
    END LOOP;
    FOR rec_age IN cur_age('关联方')
    LOOP
      output_header('RELATE_LINES');
    
      output_xml('CUSTOMER_AREA',
                 rec_age.customer_area);
      output_xml('CUSTOMER_NAME',
                 rec_age.customer_name);
      output_xml('REMAIN_AMT',
                 rec_age.remain_amt);
      output_xml('AMT_30D',
                 rec_age.amt_30d);
      output_xml('AMT_30D_PERCENT',
                 CASE WHEN rec_age.amt_30d IS NULL THEN NULL ELSE round(rec_age.amt_30d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_20_30D',
                 rec_age.amt_20_30d);
    
      output_xml('AMT_20_30D_PERCENT',
                 CASE WHEN rec_age.amt_20_30d IS NULL THEN NULL ELSE round(rec_age.amt_20_30d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_10_20D',
                 rec_age.amt_10_20d);
      output_xml('AMT_10_20D_PERCENT',
                 CASE WHEN rec_age.amt_10_20d IS NULL THEN NULL ELSE round(rec_age.amt_10_20d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_3_10D',
                 rec_age.amt_3_10d);
      output_xml('AMT_3_10D_PERCENT',
                 CASE WHEN rec_age.amt_3_10d IS NULL THEN NULL ELSE round(rec_age.amt_3_10d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_3D',
                 rec_age.amt_3d);
      output_xml('AMT_3D_PERCENT',
                 CASE WHEN rec_age.amt_3d IS NULL THEN NULL ELSE round(rec_age.amt_3d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_tail('RELATE_LINES');
    END LOOP;
    FOR rec_age IN cur_age('外部客户')
    LOOP
      output_header('OUTER_LINES');
    
      output_xml('CUSTOMER_AREA',
                 rec_age.customer_area);
      output_xml('CUSTOMER_NAME',
                 rec_age.customer_name);
      output_xml('REMAIN_AMT',
                 rec_age.remain_amt);
      output_xml('AMT_30D',
                 rec_age.amt_30d);
      output_xml('AMT_30D_PERCENT',
                 CASE WHEN rec_age.amt_30d IS NULL THEN NULL ELSE round(rec_age.amt_30d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_20_30D',
                 rec_age.amt_20_30d);
    
      output_xml('AMT_20_30D_PERCENT',
                 CASE WHEN rec_age.amt_20_30d IS NULL THEN NULL ELSE round(rec_age.amt_20_30d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_10_20D',
                 rec_age.amt_10_20d);
      output_xml('AMT_10_20D_PERCENT',
                 CASE WHEN rec_age.amt_10_20d IS NULL THEN NULL ELSE round(rec_age.amt_10_20d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_3_10D',
                 rec_age.amt_3_10d);
      output_xml('AMT_3_10D_PERCENT',
                 CASE WHEN rec_age.amt_3_10d IS NULL THEN NULL ELSE round(rec_age.amt_3_10d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_xml('AMT_3D',
                 rec_age.amt_3d);
      output_xml('AMT_3D_PERCENT',
                 CASE WHEN rec_age.amt_3d IS NULL THEN NULL ELSE round(rec_age.amt_3d / rec_age.remain_amt,
                       4) * 100 || '%' END);
      output_tail('OUTER_LINES');
    END LOOP;
  
    output_tail('HEADER');
    -- API end body
    -- end activity, include debug message hint to exit api
    x_return_status := cux_api.end_activity(p_pkg_name  => g_pkg_name,
                                            p_api_name  => l_api_name,
                                            p_commit    => p_commit,
                                            x_msg_count => x_msg_count,
                                            x_msg_data  => x_msg_data);
  EXCEPTION
    WHEN fnd_api.g_exc_error THEN
      x_return_status := cux_api.handle_exceptions(p_pkg_name       => g_pkg_name,
                                                   p_api_name       => l_api_name,
                                                   p_savepoint_name => l_savepoint_name,
                                                   p_exc_name       => cux_api.g_exc_name_error,
                                                   x_msg_count      => x_msg_count,
                                                   x_msg_data       => x_msg_data);
    WHEN fnd_api.g_exc_unexpected_error THEN
      x_return_status := cux_api.handle_exceptions(p_pkg_name       => g_pkg_name,
                                                   p_api_name       => l_api_name,
                                                   p_savepoint_name => l_savepoint_name,
                                                   p_exc_name       => cux_api.g_exc_name_unexp,
                                                   x_msg_count      => x_msg_count,
                                                   x_msg_data       => x_msg_data);
    WHEN OTHERS THEN
      x_return_status := cux_api.handle_exceptions(p_pkg_name       => g_pkg_name,
                                                   p_api_name       => l_api_name,
                                                   p_savepoint_name => l_savepoint_name,
                                                   p_exc_name       => cux_api.g_exc_name_others,
                                                   x_msg_count      => x_msg_count,
                                                   x_msg_data       => x_msg_data);
  END process_request;

  PROCEDURE main(errbuf        OUT VARCHAR2,
                 retcode       OUT VARCHAR2,
                 p_org_id      IN NUMBER,
                 p_customer_id IN NUMBER,
                 p_due_date    IN VARCHAR2) IS
    l_return_status VARCHAR2(30);
    l_msg_count     NUMBER;
    l_msg_data      VARCHAR2(2000);
  BEGIN
    retcode := '0';
    -- concurrent header log
    --hand_conc_utl.log_header;
    -- conc body  
  
    -- convert parameter data type, such as varchar2 to date
    -- l_date := fnd_conc_date.string_to_date(p_parameter1);
  
    -- call process request api
    process_request(p_init_msg_list => fnd_api.g_true,
                    p_commit        => fnd_api.g_true,
                    x_return_status => l_return_status,
                    x_msg_count     => l_msg_count,
                    x_msg_data      => l_msg_data,
                    p_org_id        => p_org_id,
                    p_customer_id   => p_customer_id,
                    p_due_date      => p_due_date);
    IF l_return_status = fnd_api.g_ret_sts_error THEN
      RAISE fnd_api.g_exc_error;
    ELSIF l_return_status = fnd_api.g_ret_sts_unexp_error THEN
      RAISE fnd_api.g_exc_unexpected_error;
    END IF;
  
    -- conc end body
    -- concurrent footer log
    --hand_conc_utl.log_footer;
  
  EXCEPTION
    WHEN fnd_api.g_exc_error THEN
      --hand_conc_utl.log_message_list;
      retcode := '1';
      fnd_msg_pub.count_and_get(p_encoded => fnd_api.g_false,
                                p_count   => l_msg_count,
                                p_data    => l_msg_data);
      IF l_msg_count > 1 THEN
        l_msg_data := fnd_msg_pub.get_detail(p_msg_index => fnd_msg_pub.g_first,
                                             p_encoded   => fnd_api.g_false);
      END IF;
      errbuf := l_msg_data;
    WHEN fnd_api.g_exc_unexpected_error THEN
      --hand_conc_utl.log_message_list;
      retcode := '2';
      fnd_msg_pub.count_and_get(p_encoded => fnd_api.g_false,
                                p_count   => l_msg_count,
                                p_data    => l_msg_data);
      IF l_msg_count > 1 THEN
        l_msg_data := fnd_msg_pub.get_detail(p_msg_index => fnd_msg_pub.g_first,
                                             p_encoded   => fnd_api.g_false);
      END IF;
      errbuf := l_msg_data;
    WHEN OTHERS THEN
      fnd_msg_pub.add_exc_msg(p_pkg_name       => g_pkg_name,
                              p_procedure_name => 'MAIN',
                              p_error_text     => substrb(SQLERRM,
                                                          1,
                                                          240));
      --hand_conc_utl.log_message_list;
      retcode := '2';
      errbuf  := SQLERRM;
  END main;
END cux_ar_advance_age_pkg;
/
