let PAGE_INDEX = 1;
// TODO: 페이지 사이즈 수정
let PAGE_SIZE = 8;
let SEARCH_GENRE = '';

let LIST = [];
let TOTAL_COUNT = 0;

var getGenre = function () {

    let queryString = location.search;

    if(queryString != undefined && queryString != '') {
        queryString = queryString.replaceAll('?', '');
        queryString = queryString.split("&");
        for (let i = 0; i < queryString.length; i++) {
            if ((queryString[i].indexOf('subId') + queryString[i].indexOf('='))  > 1) {
                SEARCH_GENRE = queryString[i].split('=')[1];
                break;
            }
        }
    }
}

var getList = function() {
    const url = '/api/tv/gets.api';
    const param = {
        pageIndex: PAGE_INDEX,
        pageSize: PAGE_SIZE,
        searchGenre: SEARCH_GENRE,
    };

    API_CALL.post(url, param, function (result, message, data) {
        if(!result) {
            alert(message);
            return false;
        }
        let tvList = data.list || [];
        TOTAL_COUNT = data.totalCount || 0;

        for (let i = 0; i < tvList.length; i++) {

            // <div className="col-lg-2">
            //     <div className="card" style="width: 15rem;">
            //         <!--                    <img src="test/test.png" class="card-img-top" alt="...">-->
            //     </div>
            //     <div>제목</div>
            // </div>

            // <div th:each="tv : ${code.tvList}" className="col-lg-2">
            //     <a th:href="@{/tv/detail/{id}(id=${tv.id})}">
            //         <div className="card" style="width: 15rem;">
            //             <img th:src="${tv.fullPosterPath}" className="card-img-top" alt="...">
            //         </div>
            //         <div style="color: white" th:text="${tv.title}"></div>
            //     </a>
            // </div>

            let append = '';
            append += '<div class="col-lg-3 mb-4">';
            append += '<a class="text-center text-decoration-none" href=" /tv/detail/' + tvList[i].id + ' ">';
            append += '<div class="card">';
            append += '<img src=" ' + tvList[i].fullPosterPath +  ' " class="card-img-top" alt="...">';
            append += '</div>';
            append += '<div style="color: white;">' + tvList[i].title + '</div>';
            append += '</a>';
            append += '</div>';

            $('#tvList').append(append);
        }
    })
}

$(function(){
    getGenre();
    getList();

    $('#moreList').on('click', function () {
        PAGE_INDEX += 1;

        if ((PAGE_INDEX - 1) * PAGE_SIZE > TOTAL_COUNT) {
            alert('더 이상 데이터가 없습니다.');
            return false;
        }

        getList();
    })
});