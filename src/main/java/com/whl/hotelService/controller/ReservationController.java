package com.whl.hotelService.controller;

import com.whl.hotelService.domain.common.dto.ReservationDto;
import com.whl.hotelService.domain.common.entity.Hotel;
import com.whl.hotelService.domain.common.entity.Room;
import com.whl.hotelService.domain.common.entity.RoomFileInfo;
import com.whl.hotelService.domain.common.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Controller
@RequestMapping(value = "reservation")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    @GetMapping(value = "select")
    public void getReservationStep1(@RequestParam(value = "hotelName") String hotelName,
                                    @RequestParam(value = "checkin")
                                    String checkin,
                                    @RequestParam(value = "checkout")
                                    String checkout,
                                    @RequestParam(value = "adultCount") int adultCount,
                                    @RequestParam(value = "childCount") int childCount,
                                    Model model) {
        int people = adultCount + childCount;
        List<Room> roomList = reservationService.getHotelsRoom(hotelName, people);

        for(Room room : roomList) {
            // 시작 날짜와 종료 날짜를 LocalDate로 변환
            LocalDate startDate = LocalDate.parse(checkin);
            LocalDate endDate = LocalDate.parse(checkout);

            // 날짜 목록을 저장할 리스트
            List<String> dateList = new ArrayList<>();

            // 시작 날짜부터 종료 날짜까지 반복하여 날짜 목록에 추가
            while (!startDate.isEqual(endDate)) {
                dateList.add(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                startDate = startDate.plusDays(1);
            }

            List<Integer> reservedRoomCountList = new ArrayList<>();
            for(String date : dateList) {
                int reservedRoomCount = reservationService.getReservedRoomCount(date, room.getId());
                reservedRoomCountList.add(reservedRoomCount);
            }
            int reservedMaxRoomCount = Collections.max(reservedRoomCountList);
            reservationService.addReservedRoomCount(room.getId(), reservedMaxRoomCount);
        }

        List<Hotel> hotelList = reservationService.getAllHotel();
        model.addAttribute("hotelList", hotelList);
        List<String> region = reservationService.getDistinctRegion();
        model.addAttribute("region", region);

        model.addAttribute("hotelName", hotelName);
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("adultCount", adultCount);
        model.addAttribute("childCount", childCount);

        // 주중, 주말 가격 계산을 위한 요일 카운트 리스트 생성 코드
        // [금, 토, 주중]
        LocalDate startDate = LocalDate.parse(checkin, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endDate = LocalDate.parse(checkout, DateTimeFormatter.ofPattern("yyyy-MM-dd")).minusDays(1);

//        System.out.println(startDate.getMonthValue()); 월 가져오기

        List<DayOfWeek> daysOfWeekList = new ArrayList<>();

        // Iterate through the dates and add the day of the week to the list
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            daysOfWeekList.add(currentDate.getDayOfWeek());
            currentDate = currentDate.plusDays(1);
        }

        DayOfWeek[] daysOfWeekBetweenDates = daysOfWeekList.toArray(new DayOfWeek[0]);

        long fridayCount = Arrays.stream(daysOfWeekBetweenDates)
                .filter(dayOfWeek -> dayOfWeek == DayOfWeek.FRIDAY)
                .count();
        long saturdayCount = Arrays.stream(daysOfWeekBetweenDates)
                .filter(dayOfWeek -> dayOfWeek == DayOfWeek.SATURDAY)
                .count();
        long weekdayCount = Arrays.stream(daysOfWeekBetweenDates)
                .filter(dayOfWeek -> dayOfWeek != DayOfWeek.FRIDAY && dayOfWeek != DayOfWeek.SATURDAY)
                .count();

        List<Long> dayCountList = new ArrayList<>();

        dayCountList.add(fridayCount);
        dayCountList.add(saturdayCount);
        dayCountList.add(weekdayCount);

        model.addAttribute("dayLength", daysOfWeekBetweenDates.length); // n박
        model.addAttribute("dayCountList", dayCountList);

        List<RoomFileInfo> mainFileList = reservationService.getAllMainFiles(hotelName);

        model.addAttribute("mainFileList", mainFileList);
    }

    @PostMapping(value = "select")
    public ResponseEntity<String> postReservationStep1(ReservationDto reservationDto) {
        boolean isInserted = reservationService.insertReservation(reservationDto);

        if (isInserted) {
            return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("FAILURE", HttpStatus.BAD_GATEWAY);
        }
    }

    @DeleteMapping(value="delete/{reservationId}")
    public ResponseEntity<String> deletePayment(@PathVariable("reservationId") String reservationId) {
        boolean isDeleted = reservationService.DeleteReservation(Integer.parseInt(reservationId));

        if(isDeleted) {
            return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("FAILURE", HttpStatus.CONFLICT);
        }
    }

}