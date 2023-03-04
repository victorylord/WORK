//package com.panda.sport.rcs.trade.aspect;
//
//import com.panda.sport.rcs.core.db.config.DatabaseContextHolder;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//@Aspect
//@Component
//@Order(-1)
//public class DataSourceAspect {
//    @Pointcut("!@annotation(com.panda.sport.rcs.core.db.annotation.Master) " +
//            "&& (execution(* com.panda.sport.rcs.trade.wrapper..*.select*(..)) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.get*(..)) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.list*(..)) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.find*(..)) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.query*(..)) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.cache*(..)) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.init*(..))) " +
//            "|| execution(* com.baomidou.mybatisplus.extension.service.IService.get*(..)) " +
//            "|| execution(* com.baomidou.mybatisplus.extension.service.IService.page*(..)) " +
//            "|| execution(* com.baomidou.mybatisplus.extension.service.IService.list*(..)) " +
//            "|| execution(* com.baomidou.mybatisplus.extension.service.IService.count*(..)) " +
//            "|| execution(* com.baomidou.mybatisplus.extension.service.IService.query*(..))")
//    public void readPointcut() {
//
//    }
//
//    @Pointcut("@annotation(com.panda.sport.rcs.core.db.annotation.Master) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.insert*(..)) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.add*(..)) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.update*(..)) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.edit*(..)) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.delete*(..)) " +
//            "|| execution(* com.panda.sport.rcs.trade.wrapper..*.remove*(..))")
//    public void writePointcut() {
//
//    }
//
//    @Before("readPointcut()")
//    public void read() {
//        DatabaseContextHolder.slave();
//    }
//
//    @Before("writePointcut()")
//    public void write() {
//        DatabaseContextHolder.master();
//    }
//
//
//}
