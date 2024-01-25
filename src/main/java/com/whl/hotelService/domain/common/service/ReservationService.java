package com.whl.hotelService.domain.common.service;

import com.whl.hotelService.domain.common.dto.ReservationDto;
import com.whl.hotelService.domain.common.entity.Hotel;
import com.whl.hotelService.domain.common.entity.Reservation;
import com.whl.hotelService.domain.common.entity.ReservedRoomCount;
import com.whl.hotelService.domain.common.entity.Room;
import com.whl.hotelService.domain.common.repository.HotelRepository;
import com.whl.hotelService.domain.common.repository.ReservationRepository;
import com.whl.hotelService.domain.common.repository.ReservedRoomCountRepository;
import com.whl.hotelService.domain.common.repository.RoomRepository;
import com.whl.hotelService.domain.user.entity.User;
import com.whl.hotelService.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservedRoomCountRepository reservedRoomCountRepository;



    @Transactional(rollbackFor = Exception.class)
    public List<Room> getHotelsRoom(String hotelname, int people) {
        return roomRepository.findByHotelHotelnameAndStandardPeopleGreaterThanEqual(hotelname, people);
    }

    @Transactional(rollbackFor = Exception.class)
    @Scheduled(fixedDelay = 1000)
    public void deleteExpiredReservations() {
//        List<Date> = reservationRepository.findCreatedAt("예약 중");
//
//        reservationRepository.deleteByCreatedAtBefore();
    }

    @Transactional(rollbackFor = Exception.class) public int getReservedRoomCount(String date, Long roomId) {
        ReservedRoomCount reservedRoomCount = reservedRoomCountRepository.findByDateAndRoomId(date, roomId);

        return (reservedRoomCount != null) ? reservedRoomCount.getReservedCount() : 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addReservedRoomCount(Long id, int reservedRoomCount) {
        Room room = roomRepository.findById(id).get();

        room.setReservedRoomCount(reservedRoomCount);

        roomRepository.save(room);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Hotel> getAllHotel() {
        return hotelRepository.findAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<String> getDistinctRegion() {
        return hotelRepository.findDistinctRegion();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean insertReservation(ReservationDto reservationDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userid = authentication.getName();

        User user = userRepository.findById(userid).get();

        Room room = roomRepository.findById(reservationDto.getRoomId()).get();

        String status = reservationDto.getStatus();

        if (user != null && room != null) {
            // Check if a reservation already exists for the user and room
            Reservation existingReservation = reservationRepository.findByUserUseridAndStatus(userid, status);

            if (existingReservation != null) {
                // Update existing reservation
                existingReservation.setCheckin(reservationDto.getCheckin());
                existingReservation.setCheckout(reservationDto.getCheckout());
                existingReservation.setRoom(room);
                existingReservation.setPeople(reservationDto.getPeople());
                existingReservation.setCreatedAt(new Date());
                existingReservation.setPrice(reservationDto.getPrice());

                reservationRepository.save(existingReservation);

            } else {
                // Create a new reservation
                Reservation reservation = new Reservation();
                reservation.setRoom(room);
                reservation.setUser(user);
                reservation.setStatus(reservationDto.getStatus());
                reservation.setCheckin(reservationDto.getCheckin());
                reservation.setCheckout(reservationDto.getCheckout());
                reservation.setPeople(reservationDto.getPeople());
                reservation.setCreatedAt(new Date());
                reservation.setPrice(reservationDto.getPrice());

                reservationRepository.save(reservation);

            }
        }
        return true;
    }


}
