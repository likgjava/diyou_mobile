<%@ page language="java" pageEncoding="UTF-8"%>
<nav class="nav-top navbar-fixed-top">
  <div class="container">
    <a onclick="history.go(-1)" class="go-back"><span class="iconfont">&#xe604;</span></a>
    <span>${agreement.title}</span>
  </div>
</nav>
<section>
  <div class="page-content">
    <div class="page-agreement">
      <div class="title">《${agreement.title}》</div>
      <div class="cont">
        ${agreement.contents}
      </div>  
    </div> 
  </div>
</section>

