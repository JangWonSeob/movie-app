package com.website.movie.biz.service.impl;

import com.website.movie.biz.component.TmdbComponent;
import com.website.movie.biz.dao.*;
import com.website.movie.biz.dto.*;
import com.website.movie.biz.model.movie.MovieData;
import com.website.movie.biz.model.movie.detail.MovieGenres;
import com.website.movie.biz.model.movie.detail.MovieKrBuyRent;
import com.website.movie.biz.model.movie.detail.MovieResultDetail;
import com.website.movie.biz.model.movie.detail.MovieVideos;
import com.website.movie.biz.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieDao movieDao;
    private final MovieWatchProviderDao movieWatchProviderDao;
    private final MovieYoutubeDao movieYoutubeDao;
    private final CodeDao codeDao;
    private final BookMarkDao bookMarkDao;


    private final TmdbComponent tmdbComponent;


    private void setGenre(List<MovieGenres> genreList, MovieDto parameter) {

        if (genreList != null) {
            String idStr = "";
            String nameStr = "";
            for (int i = 0; i < genreList.size(); i++) {
                idStr += genreList.get(i).getId();
                nameStr += genreList.get(i).getName();
                if (i < genreList.size() - 1) {
                    idStr += ",";
                    nameStr += ",";
                }
            }
            parameter.setGenreIds(idStr);
            parameter.setGenreNames(nameStr);
        }

    }

    @Override
    public List<MovieDto> index() {

        return movieDao.index();
    }

    @Override
    public List<CodeDto> main() {
        List<CodeDto> result = codeDao.selectList(CodeDto.builder().type(MovieDto.CODE_TYPE_MOVIE_GENRE).build());

        for (CodeDto x : result) {
            x.setMovieList(movieDao.main(MovieDto.builder().searchGenre(x.getSubId()).build()));
        }
        return result;
    }


    @Override
    public void getTmdbMovieData() {

        for (int i = 0; i < 10; i++) {
            List<MovieData> list = tmdbComponent.getMovieList(i + 1);

            if (list != null) {
                for (MovieData x : list) {
                    MovieResultDetail movieDetail = tmdbComponent.getMovieDetail(x.getId());
                    MovieDto parameter = MovieDto.toDto(movieDetail);
                    // 삭제
                    movieDao.delete(parameter);
                    // 공급자 삭제
                    movieWatchProviderDao.deleteByMovieId(MovieWatchProvidersDto.builder().movieId(parameter.getId()).build());
                    // YOUTUBE 삭제
                    movieYoutubeDao.deleteByMovieId(MovieYoutubeDto.builder().movieId(parameter.getId()).build());

                    // 장르 세팅
                    setGenre(movieDetail.getGenres(), parameter);
                    // 영화 추가
                    movieDao.insert(parameter);
                    if (movieDetail.getWatchProviders() != null && movieDetail.getWatchProviders().getResults() != null
                            && movieDetail.getWatchProviders().getResults().getKR() != null) {
                        if (movieDetail.getWatchProviders().getResults().getKR().getBuy() != null && movieDetail.getWatchProviders().getResults().getKR().getBuy().size() > 0) {
                            // 공급자 추가(buy)
                            for (MovieKrBuyRent y : movieDetail.getWatchProviders().getResults().getKR().getBuy()) {
                                movieWatchProviderDao.insert(MovieWatchProvidersDto.toDto(movieDetail.getId(), y, MovieWatchProvidersDto.TYPE_BUY));
                            }
                        }
                        if (movieDetail.getWatchProviders().getResults().getKR().getRent() != null && movieDetail.getWatchProviders().getResults().getKR().getRent().size() > 0) {
                            // 공급자 추가(rent)
                            for (MovieKrBuyRent z : movieDetail.getWatchProviders().getResults().getKR().getRent()) {
                                movieWatchProviderDao.insert(MovieWatchProvidersDto.toDto(movieDetail.getId(), z, MovieWatchProvidersDto.TYPE_RENT));
                            }
                        }
                    }

                    int j = 1;
                    if (movieDetail.getVideos() != null && movieDetail.getVideos().getResults() != null
                            && !movieDetail.getVideos().getResults().isEmpty()) {
                        // YOUTUBE 추가
                        for (MovieVideos y : movieDetail.getVideos().getResults()) {
                            if ("YouTube".equals(y.getSite())) {
                                movieYoutubeDao.insert(MovieYoutubeDto.toDto(y, movieDetail.getId(), j++));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<MovieDto> gets(MovieDto parameter) {

        return movieDao.selectList(parameter);
    }

    @Override
    public int totalCount(MovieDto parameter) {

        return movieDao.selectListCount(parameter);
    }

    @Override
    public MovieDto get(MovieDto parameter) {

        MovieDto result = movieDao.selectOne(parameter);

        MovieWatchProvidersDto watchProvidersDto = MovieWatchProvidersDto.builder()
                .movieId(result.getId())
                .searchProviderType(MovieWatchProvidersDto.TYPE_BUY)
                .build();

        // buy
        result.setWatchProvidersBuyList(movieWatchProviderDao.selectList(watchProvidersDto));

        // rent
        watchProvidersDto.setSearchProviderType(MovieWatchProvidersDto.TYPE_RENT);
        result.setWatchProvidersRentList(movieWatchProviderDao.selectList(watchProvidersDto));

        // YOUTUBE
        result.setWatchYoutubeList(movieYoutubeDao.selectList(MovieYoutubeDto.builder().movieId(result.getId()).build()));

        if (parameter.getLoginUserId() > 0) {
            result.setBookMarkYn(bookMarkDao.selectMyBookMark(BookMarkDto.builder().tableId(result.getId()).tableName(BookMarkDto.TABLE_NAME_MOVIE).loginUserId(parameter.getLoginUserId()).build()));
        }


        return result;
    }


}
