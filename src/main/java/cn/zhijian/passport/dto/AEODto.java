package cn.zhijian.passport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AEODto {
	Integer remainDay;
	AEOInfoDto day;
	AEOInfoDto week;
	AEOInfoDto month;

}
