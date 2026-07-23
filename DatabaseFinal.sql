USE [master]
GO
/****** Object:  Database [RemotePatientMonitoringVer2]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE DATABASE [RemotePatientMonitoringVer2]

ALTER DATABASE [RemotePatientMonitoringVer2] SET COMPATIBILITY_LEVEL = 150
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [RemotePatientMonitoringVer2].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ARITHABORT OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET  ENABLE_BROKER 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET RECOVERY FULL 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET  MULTI_USER 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET DB_CHAINING OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'RemotePatientMonitoringVer2', N'ON'
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET QUERY_STORE = OFF
GO
USE [RemotePatientMonitoringVer2]
GO
/****** Object:  Table [dbo].[accounts]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[accounts](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[email] [varchar](255) NOT NULL,
	[password_hash] [varchar](255) NOT NULL,
	[role] [varchar](20) NOT NULL,
	[is_email_verified] [bit] NOT NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
	[registration_details] [varchar](4000) NULL,
 CONSTRAINT [PK_accounts] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[alert_thresholds]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[alert_thresholds](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[hospital_id] [int] NOT NULL,
	[patient_id] [int] NULL,
	[scope] [varchar](10) NOT NULL,
	[metric_type] [varchar](50) NOT NULL,
	[glucose_hypo_threshold] [decimal](4, 2) NULL,
	[glucose_normal_max] [decimal](4, 2) NULL,
	[glucose_high_max] [decimal](4, 2) NULL,
	[systolic_normal_max] [int] NULL,
	[systolic_warning_min] [int] NULL,
	[systolic_warning_max] [int] NULL,
	[systolic_danger_min] [int] NULL,
	[systolic_danger_max] [int] NULL,
	[systolic_emergency_threshold] [int] NULL,
	[diastolic_normal_max] [int] NULL,
	[diastolic_warning_min] [int] NULL,
	[diastolic_warning_max] [int] NULL,
	[diastolic_danger_min] [int] NULL,
	[diastolic_danger_max] [int] NULL,
	[diastolic_emergency_threshold] [int] NULL,
	[created_by_doctor_id] [int] NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_alert_thresholds] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[alerts]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[alerts](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[health_log_id] [int] NULL,
	[alert_level] [int] NOT NULL,
	[alert_color] [varchar](10) NOT NULL,
	[metric_type] [varchar](30) NOT NULL,
	[metric_value] [nvarchar](200) NOT NULL,
	[threshold_violated] [nvarchar](200) NOT NULL,
	[alert_message] [nvarchar](500) NOT NULL,
	[is_resolved] [bit] NOT NULL,
	[resolved_at] [datetime2](7) NULL,
	[resolved_by_doctor_id] [int] NULL,
	[resolution_notes] [nvarchar](500) NULL,
	[triggered_at] [datetime2](7) NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_alerts] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[appointments]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[appointments](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[appointment_time] [datetime2](7) NOT NULL,
	[location] [nvarchar](255) NULL,
	[status] [varchar](15) NOT NULL,
	[appointment_type] [varchar](20) NOT NULL,
	[created_by] [varchar](10) NOT NULL,
	[patient_requested_time] [datetime2](7) NULL,
	[patient_request_reason] [nvarchar](max) NULL,
	[doctor_note] [nvarchar](max) NULL,
	[rejection_reason] [nvarchar](500) NULL,
	[reminder_sent_2days] [bit] NOT NULL,
	[completed_at] [datetime2](7) NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_appointments] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[audit_trails]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[audit_trails](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[actor_type] [varchar](50) NULL,
	[actor_id] [int] NOT NULL,
	[action] [varchar](100) NOT NULL,
	[target_table] [varchar](100) NOT NULL,
	[target_record_id] [int] NOT NULL,
	[old_value] [nvarchar](max) NULL,
	[new_value] [nvarchar](max) NULL,
	[ip_address] [varchar](100) NULL,
	[device_info] [varchar](500) NULL,
	[notes] [nvarchar](max) NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_audit_trails] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[clinical_records]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[clinical_records](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[examination_date] [datetime2](7) NOT NULL,
	[weight_kg] [decimal](5, 2) NULL,
	[height_cm] [decimal](5, 2) NULL,
	[bmi] [decimal](5, 2) NULL,
	[systolic_bp] [int] NULL,
	[diastolic_bp] [int] NULL,
	[heart_rate] [int] NULL,
	[fasting_glucose] [decimal](4, 2) NULL,
	[hba1c] [decimal](5, 2) NULL,
	[diagnosis] [nvarchar](max) NULL,
	[symptoms_noted] [nvarchar](max) NULL,
	[doctor_notes] [nvarchar](max) NULL,
	[is_initial_exam] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_clinical_records] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[change_requests]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[change_requests](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[request_type] [varchar](20) NOT NULL,
	[patient_reason] [nvarchar](max) NULL,
	[status] [varchar](15) NOT NULL,
	[doctor_response] [nvarchar](max) NULL,
	[processed_at] [datetime2](7) NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_change_requests] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[daily_health_logs]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[daily_health_logs](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[log_date] [date] NOT NULL,
	[log_time] [datetime2](7) NOT NULL,
	[log_type] [varchar](10) NOT NULL,
	[systolic_bp] [int] NULL,
	[diastolic_bp] [int] NULL,
	[heart_rate] [int] NULL,
	[glucose_level] [decimal](4, 2) NULL,
	[input_method] [varchar](10) NOT NULL,
	[image_url] [varchar](500) NULL,
	[is_ocr_validated] [bit] NOT NULL,
	[alert_level] [varchar](10) NULL,
	[patient_notes] [nvarchar](500) NULL,
	[is_alert_processed] [bit] NOT NULL,
	[alert_processed_at] [datetime2](7) NULL,
	[alert_processed_by] [int] NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_daily_health_logs] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[diet_logs]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[diet_logs](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[food_id] [int] NOT NULL,
	[log_date] [date] NOT NULL,
	[meal_type] [varchar](10) NOT NULL,
	[quantity_g] [decimal](6, 2) NOT NULL,
	[logged_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_diet_logs] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[disease_profiles]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[disease_profiles](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[profile_code] [varchar](20) NOT NULL,
	[profile_name] [nvarchar](100) NOT NULL,
	[description] [nvarchar](500) NULL,
	[requires_bp_input] [bit] NOT NULL,
	[requires_glucose_input] [bit] NOT NULL,
	[is_active] [bit] NOT NULL,
 CONSTRAINT [PK_disease_profiles] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[doctors]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[doctors](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[account_id] [int] NOT NULL,
	[hospital_id] [int] NOT NULL,
	[doctor_code] [varchar](50) NOT NULL,
	[full_name] [nvarchar](255) NOT NULL,
	[gender] [varchar](10) NULL,
	[phone] [varchar](20) NOT NULL,
	[specialty] [nvarchar](255) NULL,
	[capacity_limit] [int] NOT NULL,
	[current_patient_count] [int] NOT NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_doctors] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[emergency_guides]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[emergency_guides](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[hospital_id] [int] NOT NULL,
	[alert_level] [varchar](10) NOT NULL,
	[metric_type] [varchar](30) NOT NULL,
	[title] [nvarchar](255) NOT NULL,
	[instruction_content] [nvarchar](max) NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_emergency_guides] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[emergency_protocols]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[emergency_protocols](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[hospital_id] [int] NOT NULL,
	[condition_type] [varchar](30) NOT NULL,
	[title] [nvarchar](255) NOT NULL,
	[warning_signs] [nvarchar](max) NULL,
	[instruction_content] [nvarchar](max) NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_emergency_protocols] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[exercise_logs]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[exercise_logs](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[log_date] [date] NOT NULL,
	[exercise_type] [nvarchar](100) NOT NULL,
	[duration_minutes] [int] NULL,
	[steps_count] [int] NULL,
	[logged_at] [datetime2](7) NOT NULL,
	[calories_burned] [float] NULL,
 CONSTRAINT [PK_exercise_logs] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[foods_dictionary]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[foods_dictionary](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[food_code] [varchar](50) NOT NULL,
	[food_name] [nvarchar](255) NOT NULL,
	[english_name] [nvarchar](255) NULL,
	[water_g] [decimal](5, 2) NULL,
	[energy_kcal] [int] NULL,
	[protein_g] [decimal](5, 2) NULL,
	[lipid_g] [decimal](5, 2) NULL,
	[glucid_g] [decimal](5, 2) NULL,
	[celluloza_g] [decimal](5, 2) NULL,
	[ash_g] [decimal](5, 2) NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[hospitals]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[hospitals](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[account_id] [int] NOT NULL,
	[hospital_code] [varchar](50) NOT NULL,
	[full_name] [nvarchar](255) NOT NULL,
	[address] [nvarchar](500) NULL,
	[phone] [varchar](20) NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_hospitals] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[medication_logs]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[medication_logs](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_medication_id] [int] NOT NULL,
	[log_date] [date] NOT NULL,
	[is_taken] [bit] NOT NULL,
	[taken_at] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[menus]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[menus](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ai_generated_content] [nvarchar](max) NULL,
	[approved_at] [datetime2](6) NULL,
	[created_at] [datetime2](6) NOT NULL,
	[doctor_approved_content] [nvarchar](max) NULL,
	[is_emergency_adjustment] [bit] NOT NULL,
	[rejection_reason] [varchar](500) NULL,
	[status] [varchar](10) NOT NULL,
	[updated_at] [datetime2](6) NOT NULL,
	[week_end_date] [date] NOT NULL,
	[week_start_date] [date] NOT NULL,
	[approved_by_doctor_id] [int] NULL,
	[nutrition_rule_id] [int] NOT NULL,
	[patient_id] [int] NOT NULL,
	[treatment_plan_id] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[notifications]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[notifications](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[recipient_type] [varchar](255) NULL,
	[recipient_id] [int] NOT NULL,
	[notification_type] [varchar](255) NULL,
	[title] [nvarchar](255) NOT NULL,
	[content] [nvarchar](1000) NULL,
	[channel] [varchar](255) NULL,
	[related_entity_type] [varchar](50) NULL,
	[related_entity_id] [int] NULL,
	[status] [varchar](255) NULL,
	[is_read] [bit] NOT NULL,
	[sent_at] [datetime2](7) NULL,
	[read_at] [datetime2](7) NULL,
	[created_at] [datetime2](7) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NULL,
 CONSTRAINT [PK_notifications] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[nutrition_rules]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[nutrition_rules](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[max_calories_per_day] [int] NOT NULL,
	[max_carbs_g] [decimal](7, 2) NOT NULL,
	[max_salt_g] [decimal](6, 2) NOT NULL,
	[min_fiber_g] [decimal](6, 2) NOT NULL,
	[max_fat_g] [decimal](7, 2) NULL,
	[min_protein_g] [decimal](7, 2) NULL,
	[daily_water_ml] [int] NULL,
	[additional_notes] [nvarchar](max) NULL,
	[is_current] [bit] NOT NULL,
	[effective_from] [date] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_nutrition_rules] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[otp_codes]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[otp_codes](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[email] [varchar](255) NOT NULL,
	[otp_code] [varchar](255) NULL,
	[otp_type] [varchar](255) NULL,
	[expires_at] [datetime2](7) NOT NULL,
	[is_used] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_otp_codes] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[patient_medications]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[patient_medications](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[medicine_name] [nvarchar](255) NOT NULL,
	[dosage] [nvarchar](100) NOT NULL,
	[scheduled_time] [varchar](10) NOT NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
	[treatment_plan_id] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[patients]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[patients](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[account_id] [int] NOT NULL,
	[hospital_id] [int] NOT NULL,
	[doctor_id] [int] NULL,
	[disease_profile_id] [int] NULL,
	[patient_code] [varchar](50) NULL,
	[full_name] [nvarchar](255) NOT NULL,
	[date_of_birth] [date] NULL,
	[gender] [varchar](10) NULL,
	[phone] [varchar](20) NOT NULL,
	[address] [nvarchar](500) NULL,
	[emergency_contact_name] [nvarchar](255) NULL,
	[emergency_contact_phone] [varchar](20) NULL,
	[status] [varchar](20) NOT NULL,
	[registration_source] [varchar](20) NOT NULL,
	[is_active] [bit] NOT NULL,
	[onboarded_at] [datetime2](7) NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_patients] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[system_logs]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[system_logs](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[log_level] [varchar](10) NOT NULL,
	[module_name] [varchar](100) NOT NULL,
	[event_code] [varchar](100) NULL,
	[message] [nvarchar](max) NOT NULL,
	[related_patient_id] [int] NULL,
	[related_doctor_id] [int] NULL,
	[related_entity_type] [varchar](50) NULL,
	[related_entity_id] [int] NULL,
	[additional_data] [nvarchar](max) NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_system_logs] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[treatment_plans]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[treatment_plans](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[nutrition_rule_id] [int] NULL,
	[target_systolic_bp] [int] NULL,
	[target_diastolic_bp] [int] NULL,
	[target_fasting_glucose] [numeric](6, 2) NULL,
	[target_hba1c] [numeric](5, 2) NULL,
	[target_weight_kg] [numeric](5, 2) NULL,
	[target_steps] [int] NULL,
	[target_sleep_hours] [numeric](4, 2) NULL,
	[medical_order] [nvarchar](max) NULL,
	[exercise_goal] [nvarchar](500) NULL,
	[additional_notes] [nvarchar](max) NULL,
	[is_current] [bit] NOT NULL,
	[effective_from] [date] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
	[baseline_diastolic_bp] [int] NULL,
	[baseline_fasting_glucose] [numeric](6, 2) NULL,
	[baseline_hba1c] [numeric](5, 2) NULL,
	[baseline_systolic_bp] [int] NULL,
	[baseline_weight_kg] [numeric](5, 2) NULL,
 CONSTRAINT [PK_treatment_plans] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[water_logs]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[water_logs](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[log_date] [date] NOT NULL,
	[amount_ml] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[accounts] ON 
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (1, N'admin@bvdktrunguong-mau.vn', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'HOSPITAL_ADMIN', 1, 1, CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (2, N'bs.an@hospital.vn', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'DOCTOR', 1, 1, CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (3, N'bs.binh@hospital.vn', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'DOCTOR', 1, 1, CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (4, N'benh.nhan.1@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (5, N'benh.nhan.2@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (6, N'benh.nhan.3@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (7, N'benh.nhan.4@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), CAST(N'2026-06-27T15:58:01.7267861' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (8, N'doctor1@hospital.vn', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'DOCTOR', 1, 1, CAST(N'2026-06-27T16:13:53.1588090' AS DateTime2), CAST(N'2026-06-27T16:13:53.1588090' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (9, N'doctor2@hospital.vn', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'DOCTOR', 1, 1, CAST(N'2026-06-27T16:13:53.3593845' AS DateTime2), CAST(N'2026-06-27T16:13:53.3593845' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (10, N'doctor3@hospital.vn', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'DOCTOR', 1, 1, CAST(N'2026-06-27T16:13:53.3603962' AS DateTime2), CAST(N'2026-06-27T16:13:53.3603962' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (11, N'doctor4@hospital.vn', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'DOCTOR', 1, 1, CAST(N'2026-06-27T16:13:53.3614087' AS DateTime2), CAST(N'2026-06-27T16:13:53.3614087' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (12, N'doctor5@hospital.vn', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'DOCTOR', 1, 1, CAST(N'2026-06-27T16:13:53.3624004' AS DateTime2), CAST(N'2026-06-27T16:13:53.3624004' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (13, N'patient1@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.3634049' AS DateTime2), CAST(N'2026-06-27T16:13:53.3634049' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (14, N'patient2@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5582371' AS DateTime2), CAST(N'2026-06-27T16:13:53.5582371' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (15, N'patient3@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5607683' AS DateTime2), CAST(N'2026-06-27T16:13:53.5607683' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (16, N'patient4@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5607683' AS DateTime2), CAST(N'2026-06-27T16:13:53.5607683' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (17, N'patient5@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5617492' AS DateTime2), CAST(N'2026-06-27T16:13:53.5617492' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (18, N'patient6@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5617492' AS DateTime2), CAST(N'2026-06-27T16:13:53.5617492' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (19, N'patient7@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5627564' AS DateTime2), CAST(N'2026-06-27T16:13:53.5627564' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (20, N'patient8@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5627564' AS DateTime2), CAST(N'2026-06-27T16:13:53.5627564' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (21, N'patient9@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5638636' AS DateTime2), CAST(N'2026-06-27T16:13:53.5638636' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (22, N'patient10@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5658471' AS DateTime2), CAST(N'2026-06-27T16:13:53.5658471' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (23, N'patient11@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5658471' AS DateTime2), CAST(N'2026-06-27T16:13:53.5658471' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (24, N'patient12@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5667578' AS DateTime2), CAST(N'2026-06-27T16:13:53.5667578' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (25, N'patient13@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5667578' AS DateTime2), CAST(N'2026-06-27T16:13:53.5667578' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (26, N'patient14@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5677535' AS DateTime2), CAST(N'2026-06-27T16:13:53.5677535' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (27, N'patient15@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5688087' AS DateTime2), CAST(N'2026-06-27T16:13:53.5688087' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (28, N'patient16@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5688087' AS DateTime2), CAST(N'2026-06-27T16:13:53.5688087' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (29, N'patient17@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5697600' AS DateTime2), CAST(N'2026-06-27T16:13:53.5697600' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (30, N'patient18@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5709963' AS DateTime2), CAST(N'2026-06-27T16:13:53.5709963' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (31, N'patient19@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5720062' AS DateTime2), CAST(N'2026-06-27T16:13:53.5720062' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (32, N'patient20@gmail.com', N'$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:13:53.5720062' AS DateTime2), CAST(N'2026-06-27T16:13:53.5720062' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (33, N'patient1_73c7c092@example.com', N'hashedpassword123', N'PATIENT', 1, 1, CAST(N'2026-06-29T08:35:06.2433261' AS DateTime2), CAST(N'2026-06-29T08:35:06.2433261' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (34, N'patient2_f95e0f39@example.com', N'hashedpassword123', N'PATIENT', 1, 1, CAST(N'2026-06-29T08:35:06.3048691' AS DateTime2), CAST(N'2026-06-29T08:35:06.3048691' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (35, N'patient3_a589ab6f@example.com', N'hashedpassword123', N'PATIENT', 1, 1, CAST(N'2026-06-29T08:35:06.3119784' AS DateTime2), CAST(N'2026-06-29T08:35:06.3119784' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (36, N'patient4_a39e1f41@example.com', N'hashedpassword123', N'PATIENT', 1, 1, CAST(N'2026-06-29T08:35:06.3180598' AS DateTime2), CAST(N'2026-06-29T08:35:06.3180598' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (37, N'patient5_29dd2bbb@example.com', N'hashedpassword123', N'PATIENT', 1, 1, CAST(N'2026-06-29T08:35:06.3245088' AS DateTime2), CAST(N'2026-06-29T08:35:06.3245088' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (38, N'patient6_2bbba644@example.com', N'hashedpassword123', N'PATIENT', 1, 1, CAST(N'2026-06-29T08:35:06.3328623' AS DateTime2), CAST(N'2026-06-29T08:35:06.3328623' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (39, N'patient7_9f596ea2@example.com', N'hashedpassword123', N'PATIENT', 1, 1, CAST(N'2026-06-29T08:35:06.3386347' AS DateTime2), CAST(N'2026-06-29T08:35:06.3386347' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (40, N'patient8_9f4b7b15@example.com', N'hashedpassword123', N'PATIENT', 1, 1, CAST(N'2026-06-29T08:35:06.3440409' AS DateTime2), CAST(N'2026-06-29T08:35:06.3440409' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (41, N'patient9_ddbbd986@example.com', N'hashedpassword123', N'PATIENT', 1, 1, CAST(N'2026-06-29T08:35:06.3513594' AS DateTime2), CAST(N'2026-06-29T08:35:06.3513594' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (42, N'patient10_178c761c@example.com', N'hashedpassword123', N'PATIENT', 1, 1, CAST(N'2026-06-29T08:35:06.3563102' AS DateTime2), CAST(N'2026-06-29T08:35:06.3563102' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (43, N'domanht11@gmail.com', N'$2a$10$XDyzP.bC4qkryUGPsYETLOw6wvPxNSvIYT1ie3yX/gMdFRw/4G6P2', N'PATIENT', 0, 1, CAST(N'2026-07-12T22:43:28.0615357' AS DateTime2), CAST(N'2026-07-12T22:43:28.0615357' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (44, N'domanht13@gmail.com', N'$2a$10$cL73GgCfFe7S0C0JPCOxWOt/3/KNZUWyxF8eotXko9m08jRsTF8gW', N'PATIENT', 1, 1, CAST(N'2026-07-12T22:45:38.5627510' AS DateTime2), CAST(N'2026-07-12T22:45:38.5627510' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (45, N'domanht14@gmail.com', N'$2a$10$wm2G8awfxK4Ea1QsQe0V7eqMVKNcaj.DrsrtcM3.ia0gWcrIMrO1a', N'PATIENT', 0, 1, CAST(N'2026-07-12T22:49:22.9654816' AS DateTime2), CAST(N'2026-07-12T22:49:22.9654816' AS DateTime2), NULL)
GO
SET IDENTITY_INSERT [dbo].[accounts] OFF
GO
SET IDENTITY_INSERT [dbo].[alert_thresholds] ON 
GO
INSERT [dbo].[alert_thresholds] ([id], [hospital_id], [patient_id], [scope], [metric_type], [glucose_hypo_threshold], [glucose_normal_max], [glucose_high_max], [systolic_normal_max], [systolic_warning_min], [systolic_warning_max], [systolic_danger_min], [systolic_danger_max], [systolic_emergency_threshold], [diastolic_normal_max], [diastolic_warning_min], [diastolic_warning_max], [diastolic_danger_min], [diastolic_danger_max], [diastolic_emergency_threshold], [created_by_doctor_id], [created_at], [updated_at]) VALUES (1, 1, NULL, N'HOSPITAL', N'COMBINED', CAST(4.40 AS Decimal(4, 2)), CAST(10.00 AS Decimal(4, 2)), CAST(16.00 AS Decimal(4, 2)), 120, 130, 139, 140, 179, 180, 80, 85, 89, 90, 109, 110, NULL, CAST(N'2026-06-27T15:58:01.7367464' AS DateTime2), CAST(N'2026-06-27T15:58:01.7367464' AS DateTime2))
GO
INSERT [dbo].[alert_thresholds] ([id], [hospital_id], [patient_id], [scope], [metric_type], [glucose_hypo_threshold], [glucose_normal_max], [glucose_high_max], [systolic_normal_max], [systolic_warning_min], [systolic_warning_max], [systolic_danger_min], [systolic_danger_max], [systolic_emergency_threshold], [diastolic_normal_max], [diastolic_warning_min], [diastolic_warning_max], [diastolic_danger_min], [diastolic_danger_max], [diastolic_emergency_threshold], [created_by_doctor_id], [created_at], [updated_at]) VALUES (2, 1, 31, N'PATIENT', N'COMBINED', CAST(3.70 AS Decimal(4, 2)), CAST(9.40 AS Decimal(4, 2)), CAST(14.50 AS Decimal(4, 2)), 12, 85, 139, 140, 179, 180, 80, 85, 89, 90, 109, 110, 1, CAST(N'2026-07-14T22:58:44.4080165' AS DateTime2), CAST(N'2026-07-20T08:45:34.8524327' AS DateTime2))
GO
INSERT [dbo].[alert_thresholds] ([id], [hospital_id], [patient_id], [scope], [metric_type], [glucose_hypo_threshold], [glucose_normal_max], [glucose_high_max], [systolic_normal_max], [systolic_warning_min], [systolic_warning_max], [systolic_danger_min], [systolic_danger_max], [systolic_emergency_threshold], [diastolic_normal_max], [diastolic_warning_min], [diastolic_warning_max], [diastolic_danger_min], [diastolic_danger_max], [diastolic_emergency_threshold], [created_by_doctor_id], [created_at], [updated_at]) VALUES (3, 1, 1, N'PATIENT', N'COMBINED', CAST(4.40 AS Decimal(4, 2)), CAST(8.80 AS Decimal(4, 2)), CAST(16.00 AS Decimal(4, 2)), 120, 130, 139, 140, 179, 180, 80, 85, 89, 90, 109, 110, 1, CAST(N'2026-07-15T11:54:54.1334322' AS DateTime2), CAST(N'2026-07-15T11:54:54.1334322' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[alert_thresholds] OFF
GO
SET IDENTITY_INSERT [dbo].[alerts] ON 
GO
INSERT [dbo].[alerts] ([id], [patient_id], [doctor_id], [health_log_id], [alert_level], [alert_color], [metric_type], [metric_value], [threshold_violated], [alert_message], [is_resolved], [resolved_at], [resolved_by_doctor_id], [resolution_notes], [triggered_at], [created_at]) VALUES (3, 1, 1, NULL, 3, N'RED', N'BLOOD_PRESSURE', N'160/100', N'> 140/90', N'Huyết áp tăng quá cao (160/100)', 1, CAST(N'2026-07-07T00:12:56.9761135' AS DateTime2), 1, N'đâsdsa', CAST(N'2026-06-27T22:28:29.8633333' AS DateTime2), CAST(N'2026-06-27T22:28:29.8633333' AS DateTime2))
GO
INSERT [dbo].[alerts] ([id], [patient_id], [doctor_id], [health_log_id], [alert_level], [alert_color], [metric_type], [metric_value], [threshold_violated], [alert_message], [is_resolved], [resolved_at], [resolved_by_doctor_id], [resolution_notes], [triggered_at], [created_at]) VALUES (4, 1, 1, NULL, 2, N'ORANGE', N'GLUCOSE', N'10.5', N'> 7.0', N'Đường huyết ở mức nguy hiểm (10.5)', 1, CAST(N'2026-07-07T00:12:54.0263835' AS DateTime2), 1, N'đasa', CAST(N'2026-06-27T22:28:29.8633333' AS DateTime2), CAST(N'2026-06-27T22:28:29.8633333' AS DateTime2))
GO
INSERT [dbo].[alerts] ([id], [patient_id], [doctor_id], [health_log_id], [alert_level], [alert_color], [metric_type], [metric_value], [threshold_violated], [alert_message], [is_resolved], [resolved_at], [resolved_by_doctor_id], [resolution_notes], [triggered_at], [created_at]) VALUES (5, 2, 1, NULL, 3, N'RED', N'BLOOD_PRESSURE', N'160/100', N'> 140/90', N'Huyết áp tăng quá cao (160/100)', 1, CAST(N'2026-07-07T00:12:38.7645968' AS DateTime2), 1, N'adsads', CAST(N'2026-06-27T22:29:13.3766667' AS DateTime2), CAST(N'2026-06-27T22:29:13.3766667' AS DateTime2))
GO
INSERT [dbo].[alerts] ([id], [patient_id], [doctor_id], [health_log_id], [alert_level], [alert_color], [metric_type], [metric_value], [threshold_violated], [alert_message], [is_resolved], [resolved_at], [resolved_by_doctor_id], [resolution_notes], [triggered_at], [created_at]) VALUES (6, 2, 1, NULL, 2, N'ORANGE', N'GLUCOSE', N'10.5', N'> 7.0', N'Đường huyết ở mức nguy hiểm (10.5)', 1, CAST(N'2026-07-07T00:13:05.3191307' AS DateTime2), 1, N'dsadsa', CAST(N'2026-06-27T22:29:13.3766667' AS DateTime2), CAST(N'2026-06-27T22:29:13.3766667' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[alerts] OFF
GO
SET IDENTITY_INSERT [dbo].[appointments] ON 
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (5, 1, 1, CAST(N'2026-07-05T23:54:00.0000000' AS DateTime2), N'dsadsa232', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'đasadsadsa', NULL, 0, NULL, CAST(N'2026-06-28T23:54:59.4734488' AS DateTime2), CAST(N'2026-06-28T23:54:59.4734488' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (6, 1, 1, CAST(N'2026-06-29T00:00:00.0000000' AS DateTime2), N'adsda', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'zdasd', NULL, 0, NULL, CAST(N'2026-06-28T23:56:03.2311087' AS DateTime2), CAST(N'2026-06-28T23:56:03.2311087' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (7, 1, 1, CAST(N'2026-07-02T09:58:00.0000000' AS DateTime2), NULL, N'REJECTED', N'INITIAL', N'PATIENT', NULL, N't có bệnh mới cần khám', N'ưdasds', NULL, 0, NULL, CAST(N'2026-06-29T07:59:34.1477736' AS DateTime2), CAST(N'2026-07-16T09:06:55.0007791' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (8, 2, 1, CAST(N'2026-07-01T00:05:00.0000000' AS DateTime2), N'dsadsa232', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'đâsdasdasasdsa', NULL, 0, NULL, CAST(N'2026-06-29T09:06:53.3520647' AS DateTime2), CAST(N'2026-06-29T09:06:53.3520647' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (9, 1, 1, CAST(N'2026-06-30T09:21:00.0000000' AS DateTime2), N'dsadsadsa', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'123wdasdsada', NULL, 0, NULL, CAST(N'2026-06-29T09:21:38.5786258' AS DateTime2), CAST(N'2026-06-29T09:21:38.5786258' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (10, 1, 1, CAST(N'2026-07-01T09:21:00.0000000' AS DateTime2), N'đâsdsad', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'sadasdasdasd', NULL, 0, NULL, CAST(N'2026-06-29T09:21:47.9087129' AS DateTime2), CAST(N'2026-06-29T09:21:47.9087129' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (11, 2, 1, CAST(N'2026-07-08T11:21:00.0000000' AS DateTime2), N'đâsdasdas', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'đasadasd', NULL, 0, NULL, CAST(N'2026-06-29T09:21:57.6635128' AS DateTime2), CAST(N'2026-06-29T09:21:57.6635128' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (12, 1, 1, CAST(N'2026-07-03T23:03:00.0000000' AS DateTime2), N'Phòng khám Test Tự Động 102', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'Bệnh nhân cần nhịn ăn sáng trước khi tái khám (Auto Test).', NULL, 0, NULL, CAST(N'2026-07-02T23:03:05.6505711' AS DateTime2), CAST(N'2026-07-02T23:03:05.6505711' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (13, 1, 1, CAST(N'2026-07-04T10:08:00.0000000' AS DateTime2), N'Phòng khám Test Tự Động 102', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'Bệnh nhân cần nhịn ăn sáng trước khi tái khám (Auto Test).', NULL, 0, NULL, CAST(N'2026-07-03T10:08:34.9150949' AS DateTime2), CAST(N'2026-07-03T10:08:34.9150949' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (14, 1, 1, CAST(N'2026-07-04T10:10:00.0000000' AS DateTime2), N'Phòng khám Test Tự Động 102', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'Bệnh nhân cần nhịn ăn sáng trước khi tái khám (Auto Test).', NULL, 0, NULL, CAST(N'2026-07-03T10:10:52.4950665' AS DateTime2), CAST(N'2026-07-03T10:10:52.4950665' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (15, 1, 1, CAST(N'2026-07-11T16:07:00.0000000' AS DateTime2), N'sdsdasd', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'sdasdsdsad', NULL, 0, NULL, CAST(N'2026-07-10T16:08:10.7135953' AS DateTime2), CAST(N'2026-07-10T16:08:10.7135953' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (16, 1, 1, CAST(N'2026-07-19T16:23:00.0000000' AS DateTime2), N'dsdsadsa', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'dsadsadasd', NULL, 0, NULL, CAST(N'2026-07-10T16:24:03.3251021' AS DateTime2), CAST(N'2026-07-10T16:24:03.3251021' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (17, 1, 1, CAST(N'2026-07-17T16:24:00.0000000' AS DateTime2), N'sadasd', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'dsadsad', NULL, 0, NULL, CAST(N'2026-07-10T16:24:29.7870126' AS DateTime2), CAST(N'2026-07-10T16:24:29.7870126' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (18, 1, 1, CAST(N'2026-07-25T16:43:00.0000000' AS DateTime2), N'dsad', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'đâs', NULL, 0, NULL, CAST(N'2026-07-10T16:43:44.6868178' AS DateTime2), CAST(N'2026-07-10T16:43:44.6868178' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (19, 1, 1, CAST(N'2026-07-30T09:00:00.0000000' AS DateTime2), N'đá', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'dsadsadsa', NULL, 0, NULL, CAST(N'2026-07-13T09:00:49.3076580' AS DateTime2), CAST(N'2026-07-13T09:00:49.3086609' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (20, 1, 1, CAST(N'2026-07-14T09:01:00.0000000' AS DateTime2), NULL, N'REJECTED', N'CHECKUP', N'PATIENT', NULL, N'dsasas', N'k rah', NULL, 0, NULL, CAST(N'2026-07-13T09:02:11.7998996' AS DateTime2), CAST(N'2026-07-13T09:02:54.7093047' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (21, 1, 1, CAST(N'2026-07-15T09:24:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'dfsdfds', NULL, NULL, 0, NULL, CAST(N'2026-07-13T09:24:56.5655634' AS DateTime2), CAST(N'2026-07-13T09:25:27.6258568' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (22, 1, 1, CAST(N'2026-07-15T09:13:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'INITIAL', N'PATIENT', NULL, N'aSADSADSADSA', NULL, NULL, 0, NULL, CAST(N'2026-07-13T23:14:03.0004832' AS DateTime2), CAST(N'2026-07-13T23:17:16.8453698' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (23, 1, 1, CAST(N'2026-07-16T11:14:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'SDASDSADASD', NULL, NULL, 0, NULL, CAST(N'2026-07-13T23:14:15.5092188' AS DateTime2), CAST(N'2026-07-13T23:18:30.1997768' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (24, 1, 1, CAST(N'2026-07-17T11:14:00.0000000' AS DateTime2), NULL, N'REJECTED', N'CHECKUP', N'PATIENT', NULL, N'DSADSADSADASDASDAS', N'Tìm kiếm trên Facebook
Menu trên Facebook
Lối tắt của bạn
Quyền riêng tư  · Điều khoản  · Quảng cáo  · Lựa chọn quảng cáo   · Cookie  · 
Trang chủ
Tạo bài viết
Tin
Bài viết trên Bảng feed
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Thanh lý vợt', NULL, 0, NULL, CAST(N'2026-07-13T23:14:25.0314765' AS DateTime2), CAST(N'2026-07-13T23:33:01.5499429' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (25, 1, 1, CAST(N'2026-07-21T11:14:00.0000000' AS DateTime2), NULL, N'REJECTED', N'CHECKUP', N'PATIENT', NULL, N'DSADSDS2132312312312312', N'Doctor has an unexpected surgery', NULL, 0, NULL, CAST(N'2026-07-13T23:14:58.3020926' AS DateTime2), CAST(N'2026-07-14T01:43:46.0129068' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (26, 1, 1, CAST(N'2026-07-22T11:15:00.0000000' AS DateTime2), NULL, N'REJECTED', N'INITIAL', N'PATIENT', NULL, N'SADAS', N'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', NULL, 0, NULL, CAST(N'2026-07-13T23:15:57.4565220' AS DateTime2), CAST(N'2026-07-14T01:43:52.9691366' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (27, 1, 1, CAST(N'2026-07-14T08:13:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'sdasdsadsdasd', NULL, NULL, 0, NULL, CAST(N'2026-07-14T01:13:21.1679220' AS DateTime2), CAST(N'2026-07-14T01:13:47.5704386' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (28, 1, 1, CAST(N'2026-07-15T08:16:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'đasadas', NULL, NULL, 0, NULL, CAST(N'2026-07-14T01:13:35.4844293' AS DateTime2), CAST(N'2026-07-14T01:14:20.7620320' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (29, 1, 1, CAST(N'2026-07-14T08:29:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'dsasdsa', NULL, NULL, 0, NULL, CAST(N'2026-07-14T01:29:36.4743100' AS DateTime2), CAST(N'2026-07-14T01:31:43.7966738' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (30, 1, 1, CAST(N'2026-07-15T08:29:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'dsadsad', NULL, NULL, 0, NULL, CAST(N'2026-07-14T01:29:45.3746385' AS DateTime2), CAST(N'2026-07-14T01:42:54.0204911' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (31, 1, 1, CAST(N'2026-07-16T08:29:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'đasad', NULL, NULL, 0, NULL, CAST(N'2026-07-14T01:29:53.7060353' AS DateTime2), CAST(N'2026-07-14T01:43:27.5339225' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (32, 1, 1, CAST(N'2026-07-17T08:29:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'dsadsadsad', NULL, NULL, 0, NULL, CAST(N'2026-07-14T01:30:03.2235009' AS DateTime2), CAST(N'2026-07-14T01:43:42.5004919' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (33, 1, 1, CAST(N'2026-07-14T09:30:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'đâsdsa', NULL, NULL, 0, NULL, CAST(N'2026-07-14T01:30:37.5731981' AS DateTime2), CAST(N'2026-07-14T01:32:17.3619450' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (34, 1, 1, CAST(N'2026-07-14T10:30:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'dsadsa', NULL, NULL, 0, NULL, CAST(N'2026-07-14T01:30:51.3224865' AS DateTime2), CAST(N'2026-07-14T01:32:32.4603987' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (35, 1, 1, CAST(N'2026-07-14T08:52:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'dsa', NULL, NULL, 0, NULL, CAST(N'2026-07-14T01:52:34.9010344' AS DateTime2), CAST(N'2026-07-14T01:54:26.3193352' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (36, 1, 1, CAST(N'2026-07-15T08:52:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'dsada', NULL, NULL, 0, NULL, CAST(N'2026-07-14T01:52:45.1349103' AS DateTime2), CAST(N'2026-07-14T01:55:04.6687821' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (37, 1, 1, CAST(N'2026-07-16T08:52:00.0000000' AS DateTime2), NULL, N'REJECTED', N'CHECKUP', N'PATIENT', NULL, N'dsad', N'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', NULL, 0, NULL, CAST(N'2026-07-14T01:52:52.7859579' AS DateTime2), CAST(N'2026-07-14T01:55:14.3698312' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (38, 1, 1, CAST(N'2026-07-17T08:52:00.0000000' AS DateTime2), NULL, N'REJECTED', N'CHECKUP', N'PATIENT', NULL, N'đasadsa', N'Doctor has an unexpected surgery - 1783968943393', NULL, 0, NULL, CAST(N'2026-07-14T01:53:02.0861156' AS DateTime2), CAST(N'2026-07-14T01:55:43.8180531' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (39, 1, 1, CAST(N'2026-07-15T09:53:00.0000000' AS DateTime2), NULL, N'REJECTED', N'CHECKUP', N'PATIENT', NULL, N'dsads', N'Doctor has an unexpected surgery', NULL, 0, NULL, CAST(N'2026-07-14T01:53:28.3822446' AS DateTime2), CAST(N'2026-07-14T01:55:07.9552197' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (40, 1, 1, CAST(N'2026-07-14T10:53:00.0000000' AS DateTime2), NULL, N'ACCEPTED', N'CHECKUP', N'PATIENT', NULL, N'sadsdas', NULL, NULL, 0, NULL, CAST(N'2026-07-14T01:53:38.6008903' AS DateTime2), CAST(N'2026-07-14T01:54:59.7746975' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (41, 1, 1, CAST(N'2026-07-20T10:38:00.0000000' AS DateTime2), N'adsda', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'61515', NULL, 0, NULL, CAST(N'2026-07-20T07:39:10.6342297' AS DateTime2), CAST(N'2026-07-20T07:39:10.6342297' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (42, 1, 1, CAST(N'2026-07-20T10:39:00.0000000' AS DateTime2), NULL, N'PENDING', N'CHECKUP', N'PATIENT', NULL, N'dsadas', NULL, NULL, 0, NULL, CAST(N'2026-07-20T07:40:04.6845126' AS DateTime2), CAST(N'2026-07-20T07:40:04.6845126' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (43, 2, 1, CAST(N'2026-07-20T10:22:00.0000000' AS DateTime2), N'dsadsa232', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'sđsad', NULL, 0, NULL, CAST(N'2026-07-20T08:23:29.4053021' AS DateTime2), CAST(N'2026-07-20T08:23:29.4053021' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (44, 2, 1, CAST(N'2026-07-20T12:25:00.0000000' AS DateTime2), N'dsads', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'dsadas', NULL, 0, NULL, CAST(N'2026-07-20T08:25:37.9347392' AS DateTime2), CAST(N'2026-07-20T08:25:37.9347392' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (45, 2, 1, CAST(N'2026-07-20T23:45:00.0000000' AS DateTime2), N'dá', N'ACCEPTED', N'FOLLOWUP', N'DOCTOR', NULL, NULL, N'dsad', NULL, 0, NULL, CAST(N'2026-07-20T08:45:47.2577412' AS DateTime2), CAST(N'2026-07-20T08:45:47.2577412' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[appointments] OFF
GO
SET IDENTITY_INSERT [dbo].[audit_trails] ON 
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (1, N'DOCTOR', 1, N'RESOLVE_ALERT', N'alerts', 5, NULL, N'{"id":5,"patient":{"id":2,"account":{"id":14,"email":"patient2@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,558237100],"updatedAt":[2026,6,27,16,13,53,558237100]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":3,"profileCode":"BOTH","profileName":"Đồng mắc (Cả hai bệnh)","description":"Bệnh nhân bắt buộc phải nhập đầy đủ cả hai nhóm chỉ số huyết áp và glucose.","requiresBpInput":true,"requiresGlucoseInput":true,"isActive":true},"patientCode":"PAT002","fullName":"Bệnh nhân 2","dateOfBirth":null,"gender":null,"phone":"080000002","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,559741700],"updatedAt":[2026,6,27,16,13,53,559741700]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"healthLog":null,"alertLevel":3,"alertColor":"RED","metricType":"BLOOD_PRESSURE","metricValue":"160/100","thresholdViolated":"> 140/90","alertMessage":"Huyết áp tăng quá cao (160/100)","isResolved":true,"resolvedAt":[2026,7,7,0,12,38,764596800],"resolvedByDoctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"resolutionNotes":"adsads","triggeredAt":[2026,6,27,22,29,13,376666700],"createdAt":[2026,6,27,22,29,13,376666700]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 xử lý cảnh báo với ghi chú: adsads', CAST(N'2026-07-07T00:12:38.8659487' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (2, N'DOCTOR', 1, N'RESOLVE_ALERT', N'alerts', 4, NULL, N'{"id":4,"patient":{"id":1,"account":{"id":13,"email":"patient1@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":3,"profileCode":"BOTH","profileName":"Đồng mắc (Cả hai bệnh)","description":"Bệnh nhân bắt buộc phải nhập đầy đủ cả hai nhóm chỉ số huyết áp và glucose.","requiresBpInput":true,"requiresGlucoseInput":true,"isActive":true},"patientCode":"PAT001","fullName":"Bệnh nhân 1","dateOfBirth":null,"gender":null,"phone":"080000001","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"healthLog":null,"alertLevel":2,"alertColor":"ORANGE","metricType":"GLUCOSE","metricValue":"10.5","thresholdViolated":"> 7.0","alertMessage":"Đường huyết ở mức nguy hiểm (10.5)","isResolved":true,"resolvedAt":[2026,7,7,0,12,54,26383500],"resolvedByDoctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"resolutionNotes":"đasa","triggeredAt":[2026,6,27,22,28,29,863333300],"createdAt":[2026,6,27,22,28,29,863333300]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 xử lý cảnh báo với ghi chú: đasa', CAST(N'2026-07-07T00:12:54.0514900' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (3, N'DOCTOR', 1, N'RESOLVE_ALERT', N'alerts', 3, NULL, N'{"id":3,"patient":{"id":1,"account":{"id":13,"email":"patient1@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":3,"profileCode":"BOTH","profileName":"Đồng mắc (Cả hai bệnh)","description":"Bệnh nhân bắt buộc phải nhập đầy đủ cả hai nhóm chỉ số huyết áp và glucose.","requiresBpInput":true,"requiresGlucoseInput":true,"isActive":true},"patientCode":"PAT001","fullName":"Bệnh nhân 1","dateOfBirth":null,"gender":null,"phone":"080000001","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"healthLog":null,"alertLevel":3,"alertColor":"RED","metricType":"BLOOD_PRESSURE","metricValue":"160/100","thresholdViolated":"> 140/90","alertMessage":"Huyết áp tăng quá cao (160/100)","isResolved":true,"resolvedAt":[2026,7,7,0,12,56,976113500],"resolvedByDoctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"resolutionNotes":"đâsdsa","triggeredAt":[2026,6,27,22,28,29,863333300],"createdAt":[2026,6,27,22,28,29,863333300]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 xử lý cảnh báo với ghi chú: đâsdsa', CAST(N'2026-07-07T00:12:56.9901141' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (4, N'DOCTOR', 1, N'RESOLVE_ALERT', N'alerts', 6, NULL, N'{"id":6,"patient":{"id":2,"account":{"id":14,"email":"patient2@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,558237100],"updatedAt":[2026,6,27,16,13,53,558237100]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":3,"profileCode":"BOTH","profileName":"Đồng mắc (Cả hai bệnh)","description":"Bệnh nhân bắt buộc phải nhập đầy đủ cả hai nhóm chỉ số huyết áp và glucose.","requiresBpInput":true,"requiresGlucoseInput":true,"isActive":true},"patientCode":"PAT002","fullName":"Bệnh nhân 2","dateOfBirth":null,"gender":null,"phone":"080000002","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,559741700],"updatedAt":[2026,6,27,16,13,53,559741700]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"healthLog":null,"alertLevel":2,"alertColor":"ORANGE","metricType":"GLUCOSE","metricValue":"10.5","thresholdViolated":"> 7.0","alertMessage":"Đường huyết ở mức nguy hiểm (10.5)","isResolved":true,"resolvedAt":[2026,7,7,0,13,5,319130700],"resolvedByDoctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"resolutionNotes":"dsadsa","triggeredAt":[2026,6,27,22,29,13,376666700],"createdAt":[2026,6,27,22,29,13,376666700]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 xử lý cảnh báo với ghi chú: dsadsa', CAST(N'2026-07-07T00:13:05.3370837' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (5, N'DOCTOR', 1, N'CREATE_TREATMENT_PLAN', N'treatment_plans', 17, NULL, N'{"id":17,"patient":{"id":30,"account":{"id":42,"email":"patient10_178c761c@example.com","passwordHash":"hashedpassword123","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,29,8,35,6,356310200],"updatedAt":[2026,6,29,8,35,6,356310200]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":1,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PT-178c761c","fullName":"Bệnh nhân Test 10","dateOfBirth":null,"gender":null,"phone":"0987178c76","address":"Địa chỉ 10","emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,29,8,35,6,359309700],"updatedAt":[2026,7,7,15,14,10,298929500]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"nutritionRule":{"id":17,"patient":{"id":30,"account":{"id":42,"email":"patient10_178c761c@example.com","passwordHash":"hashedpassword123","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,29,8,35,6,356310200],"updatedAt":[2026,6,29,8,35,6,356310200]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":1,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PT-178c761c","fullName":"Bệnh nhân Test 10","dateOfBirth":null,"gender":null,"phone":"0987178c76","address":"Địa chỉ 10","emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,29,8,35,6,359309700],"updatedAt":[2026,7,7,15,14,10,298929500]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"maxCaloriesPerDay":2000,"maxCarbsG":250,"maxSaltG":5,"minFiberG":25,"maxFatG":10,"minProteinG":10,"dailyWaterMl":2000,"additionalNotes":"","isCurrent":true,"effectiveFrom":[2026,7,7],"createdAt":[2026,7,7,15,14,10,298929500],"updatedAt":[2026,7,7,15,14,10,298929500]},"targetSystolicBp":100,"targetDiastolicBp":33,"targetFastingGlucose":10,"targetHba1c":10,"targetWeightKg":10,"targetSteps":null,"targetSleepHours":null,"baselineSystolicBp":80,"baselineDiastolicBp":30,"baselineFastingGlucose":10,"baselineHba1c":10,"baselineWeightKg":10,"medicalOrder":"10","exerciseGoal":"10","additionalNotes":"10","isCurrent":true,"effectiveFrom":[2026,7,7],"createdAt":[2026,7,7,15,14,10,298929500],"updatedAt":[2026,7,7,15,14,10,298929500]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 tạo/cập nhật phác đồ mới cho bệnh nhân Bệnh nhân Test 10', CAST(N'2026-07-07T15:14:10.3739004' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (6, N'DOCTOR', 1, N'CREATE_TREATMENT_PLAN', N'treatment_plans', 18, NULL, N'{"id":18,"patient":{"id":2,"account":{"id":14,"email":"patient2@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,558237100],"updatedAt":[2026,6,27,16,13,53,558237100]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":3,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PAT002","fullName":"Bệnh nhân 2","dateOfBirth":null,"gender":null,"phone":"080000002","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,559741700],"updatedAt":[2026,6,27,16,13,53,559741700]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"nutritionRule":{"id":18,"patient":{"id":2,"account":{"id":14,"email":"patient2@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,558237100],"updatedAt":[2026,6,27,16,13,53,558237100]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":3,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PAT002","fullName":"Bệnh nhân 2","dateOfBirth":null,"gender":null,"phone":"080000002","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,559741700],"updatedAt":[2026,6,27,16,13,53,559741700]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"maxCaloriesPerDay":1550,"maxCarbsG":1.00,"maxSaltG":1.00,"minFiberG":1.00,"maxFatG":1.00,"minProteinG":1.00,"dailyWaterMl":300,"additionalNotes":"1","isCurrent":true,"effectiveFrom":[2026,7,7],"createdAt":[2026,7,7,15,23,45,541028900],"updatedAt":[2026,7,7,15,23,45,541028900]},"targetSystolicBp":80,"targetDiastolicBp":30,"targetFastingGlucose":10,"targetHba1c":10,"targetWeightKg":10,"targetSteps":null,"targetSleepHours":null,"baselineSystolicBp":100,"baselineDiastolicBp":30,"baselineFastingGlucose":20,"baselineHba1c":10,"baselineWeightKg":10,"medicalOrder":"dsadsad","exerciseGoal":"1đâsdasdsad","additionalNotes":"1sadasdasdasdsa","isCurrent":true,"effectiveFrom":[2026,7,7],"createdAt":[2026,7,7,15,23,45,541028900],"updatedAt":[2026,7,7,15,23,45,541028900]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 tạo/cập nhật phác đồ mới cho bệnh nhân Bệnh nhân 2', CAST(N'2026-07-07T15:23:45.6300303' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (7, N'DOCTOR', 1, N'CREATE_TREATMENT_PLAN', N'treatment_plans', 19, NULL, N'{"id":19,"patient":{"id":1,"account":{"id":13,"email":"patient1@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":3,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PAT001","fullName":"Bệnh nhân 1","dateOfBirth":null,"gender":null,"phone":"080000001","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"nutritionRule":{"id":19,"patient":{"id":1,"account":{"id":13,"email":"patient1@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":3,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PAT001","fullName":"Bệnh nhân 1","dateOfBirth":null,"gender":null,"phone":"080000001","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"maxCaloriesPerDay":2000,"maxCarbsG":250.00,"maxSaltG":5.00,"minFiberG":25.00,"maxFatG":1.00,"minProteinG":1.00,"dailyWaterMl":2000,"additionalNotes":"dsadsadsadsadsadsadsdsa","isCurrent":true,"effectiveFrom":[2026,7,7],"createdAt":[2026,7,7,15,27,41,57307100],"updatedAt":[2026,7,7,15,27,41,57307100]},"targetSystolicBp":100,"targetDiastolicBp":30,"targetFastingGlucose":20,"targetHba1c":10,"targetWeightKg":10,"targetSteps":null,"targetSleepHours":null,"baselineSystolicBp":120,"baselineDiastolicBp":40,"baselineFastingGlucose":20,"baselineHba1c":20,"baselineWeightKg":20,"medicalOrder":"sadsad","exerciseGoal":"dsadsad","additionalNotes":"sadsadsadsa","isCurrent":true,"effectiveFrom":[2026,7,7],"createdAt":[2026,7,7,15,27,41,57307100],"updatedAt":[2026,7,7,15,27,41,57307100]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 tạo/cập nhật phác đồ mới cho bệnh nhân Bệnh nhân 1', CAST(N'2026-07-07T15:27:41.1164476' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (8, N'DOCTOR', 1, N'PROCESS_CHANGE_REQUEST', N'change_requests', 4, NULL, N'{"id":4,"patient":{"id":1,"account":{"id":13,"email":"patient1@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":3,"profileCode":"BOTH","profileName":"Đồng mắc (Cả hai bệnh)","description":"Bệnh nhân bắt buộc phải nhập đầy đủ cả hai nhóm chỉ số huyết áp và glucose.","requiresBpInput":true,"requiresGlucoseInput":true,"isActive":true},"patientCode":"PAT001","fullName":"Bệnh nhân 1","dateOfBirth":null,"gender":null,"phone":"080000001","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"requestType":"SCHEDULE","patientReason":"đâs","status":"REJECTED","doctorResponse":"dsad","processedAt":[2026,7,10,16,8,55,60077200],"createdAt":[2026,6,29,9,7,56,455335500],"updatedAt":[2026,7,10,16,8,55,60077200]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ REJECT yêu cầu thay đổi với ghi chú: dsad', CAST(N'2026-07-10T16:08:55.1211064' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (9, N'DOCTOR', 1, N'PROCESS_CHANGE_REQUEST', N'change_requests', 5, NULL, N'{"id":5,"patient":{"id":1,"account":{"id":13,"email":"patient1@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":3,"profileCode":"BOTH","profileName":"Đồng mắc (Cả hai bệnh)","description":"Bệnh nhân bắt buộc phải nhập đầy đủ cả hai nhóm chỉ số huyết áp và glucose.","requiresBpInput":true,"requiresGlucoseInput":true,"isActive":true},"patientCode":"PAT001","fullName":"Bệnh nhân 1","dateOfBirth":null,"gender":null,"phone":"080000001","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"requestType":"SCHEDULE","patientReason":"đâs","status":"REJECTED","doctorResponse":"dsadsa","processedAt":[2026,7,10,16,8,57,777678400],"createdAt":[2026,6,29,9,8,6,957897400],"updatedAt":[2026,7,10,16,8,57,777678400]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ REJECT yêu cầu thay đổi với ghi chú: dsadsa', CAST(N'2026-07-10T16:08:57.7988171' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (10, N'DOCTOR', 1, N'CREATE_TREATMENT_PLAN', N'treatment_plans', 20, NULL, N'{"id":20,"patient":{"id":30,"account":{"id":42,"email":"patient10_178c761c@example.com","passwordHash":"hashedpassword123","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,29,8,35,6,356310200],"updatedAt":[2026,6,29,8,35,6,356310200]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":1,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PT-178c761c","fullName":"Bệnh nhân Test 10","dateOfBirth":null,"gender":null,"phone":"0987178c76","address":"Địa chỉ 10","emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,29,8,35,6,359309700],"updatedAt":[2026,7,7,15,14,10,298929500]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"nutritionRule":{"id":20,"patient":{"id":30,"account":{"id":42,"email":"patient10_178c761c@example.com","passwordHash":"hashedpassword123","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,29,8,35,6,356310200],"updatedAt":[2026,6,29,8,35,6,356310200]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"diseaseProfile":{"id":1,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PT-178c761c","fullName":"Bệnh nhân Test 10","dateOfBirth":null,"gender":null,"phone":"0987178c76","address":"Địa chỉ 10","emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,29,8,35,6,359309700],"updatedAt":[2026,7,7,15,14,10,298929500]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":4,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,6,27,16,13,53,162351200]},"maxCaloriesPerDay":2000,"maxCarbsG":250.00,"maxSaltG":5.00,"minFiberG":25.00,"maxFatG":10.00,"minProteinG":10.00,"dailyWaterMl":2000,"additionalNotes":"","isCurrent":true,"effectiveFrom":[2026,7,10],"createdAt":[2026,7,10,21,28,51,738234700],"updatedAt":[2026,7,10,21,28,51,738234700]},"targetSystolicBp":100,"targetDiastolicBp":33,"targetFastingGlucose":10.00,"targetHba1c":10.00,"targetWeightKg":10.00,"targetSteps":null,"targetSleepHours":null,"baselineSystolicBp":80,"baselineDiastolicBp":30,"baselineFastingGlucose":10.00,"baselineHba1c":10.00,"baselineWeightKg":10.00,"medicalOrder":"10","exerciseGoal":"10","additionalNotes":"10","isCurrent":true,"effectiveFrom":[2026,7,10],"createdAt":[2026,7,10,21,28,51,738234700],"updatedAt":[2026,7,10,21,28,51,738234700]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 tạo/cập nhật phác đồ mới cho bệnh nhân Bệnh nhân Test 10', CAST(N'2026-07-10T21:28:51.9139238' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (11, N'DOCTOR', 1, N'ASSIGN_PATIENT_TO_DOCTOR', N'patients', 32, NULL, N'{"doctor_id":1, "status":"TREATING"}', NULL, NULL, NULL, CAST(N'2026-07-12T22:50:15.8271147' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (12, N'DOCTOR', 1, N'ASSIGN_PATIENT_TO_DOCTOR', N'patients', 31, NULL, N'{"doctor_id":1, "status":"TREATING"}', NULL, NULL, NULL, CAST(N'2026-07-13T09:44:59.5199458' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (13, N'DOCTOR', 1, N'PROCESS_CHANGE_REQUEST', N'change_requests', 7, NULL, N'{"id":7,"patient":{"id":1,"account":{"id":13,"email":"patient1@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"diseaseProfile":{"id":3,"profileCode":"BOTH","profileName":"Đồng mắc (Cả hai bệnh)","description":"Bệnh nhân bắt buộc phải nhập đầy đủ cả hai nhóm chỉ số huyết áp và glucose.","requiresBpInput":true,"requiresGlucoseInput":true,"isActive":true},"patientCode":"PAT001","fullName":"Bệnh nhân 1","dateOfBirth":null,"gender":null,"phone":"080000001","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"requestType":"TREATMENT_PLAN","patientReason":"dsadasdasdadsadadsaadasqqqqqqqqqqqqqqqqqqqqq","status":"REJECTED","doctorResponse":"dsadsa","processedAt":[2026,7,13,10,10,34,143311400],"createdAt":[2026,7,13,9,16,24,963848600],"updatedAt":[2026,7,13,10,10,34,143311400]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ REJECT yêu cầu thay đổi với ghi chú: dsadsa', CAST(N'2026-07-13T10:10:34.1818352' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (14, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-14T22:58:44.4973306' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (15, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-14T23:04:32.2717133' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (16, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-14T23:04:36.8813804' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (17, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-14T23:04:56.9007497' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (18, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-14T23:06:32.9549396' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (19, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-14T23:10:37.2950657' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (20, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-14T23:10:39.7490566' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (21, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-14T23:14:39.9269817' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (22, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-14T23:14:51.6064850' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (23, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-15T10:40:52.8017307' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (24, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-15T10:41:13.1344730' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (25, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 3, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân Bệnh nhân 1', CAST(N'2026-07-15T11:54:54.1810885' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (26, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-18T14:21:44.1322493' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (27, N'DOCTOR', 1, N'CREATE_TREATMENT_PLAN', N'treatment_plans', 21, NULL, N'{"id":21,"patient":{"id":31,"account":{"id":43,"email":"domanht11@gmail.com","passwordHash":"$2a$10$XDyzP.bC4qkryUGPsYETLOw6wvPxNSvIYT1ie3yX/gMdFRw/4G6P2","role":"PATIENT","isEmailVerified":false,"isActive":true,"registrationDetails":null,"createdAt":[2026,7,12,22,43,28,61535700],"updatedAt":[2026,7,12,22,43,28,61535700]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"diseaseProfile":{"id":1,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PAT021","fullName":"nguyen van muoi","dateOfBirth":[2009,12,12],"gender":"MALE","phone":"0913231111","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":[2026,7,13,9,44,59,517938100],"createdAt":[2026,7,12,22,43,28,378820200],"updatedAt":[2026,7,13,9,44,59,517938100]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"nutritionRule":{"id":21,"patient":{"id":31,"account":{"id":43,"email":"domanht11@gmail.com","passwordHash":"$2a$10$XDyzP.bC4qkryUGPsYETLOw6wvPxNSvIYT1ie3yX/gMdFRw/4G6P2","role":"PATIENT","isEmailVerified":false,"isActive":true,"registrationDetails":null,"createdAt":[2026,7,12,22,43,28,61535700],"updatedAt":[2026,7,12,22,43,28,61535700]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"diseaseProfile":{"id":1,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PAT021","fullName":"nguyen van muoi","dateOfBirth":[2009,12,12],"gender":"MALE","phone":"0913231111","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":[2026,7,13,9,44,59,517938100],"createdAt":[2026,7,12,22,43,28,378820200],"updatedAt":[2026,7,13,9,44,59,517938100]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"maxCaloriesPerDay":2000,"maxCarbsG":250,"maxSaltG":5,"minFiberG":25,"maxFatG":7,"minProteinG":11,"dailyWaterMl":2000,"additionalNotes":"","isCurrent":true,"effectiveFrom":[2026,7,18],"createdAt":[2026,7,18,14,22,47,531362800],"updatedAt":[2026,7,18,14,22,47,531362800]},"targetSystolicBp":null,"targetDiastolicBp":null,"targetFastingGlucose":null,"targetHba1c":null,"targetWeightKg":null,"targetSteps":null,"targetSleepHours":null,"baselineSystolicBp":null,"baselineDiastolicBp":null,"baselineFastingGlucose":null,"baselineHba1c":null,"baselineWeightKg":null,"medicalOrder":"","exerciseGoal":"","additionalNotes":"","isCurrent":true,"effectiveFrom":[2026,7,18],"createdAt":[2026,7,18,14,22,47,531362800],"updatedAt":[2026,7,18,14,22,47,531362800]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 tạo/cập nhật phác đồ mới cho bệnh nhân nguyen van muoi', CAST(N'2026-07-18T14:22:47.6187969' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (28, N'DOCTOR', 1, N'CREATE_TREATMENT_PLAN', N'treatment_plans', 22, NULL, N'{"id":22,"patient":{"id":31,"account":{"id":43,"email":"domanht11@gmail.com","passwordHash":"$2a$10$XDyzP.bC4qkryUGPsYETLOw6wvPxNSvIYT1ie3yX/gMdFRw/4G6P2","role":"PATIENT","isEmailVerified":false,"isActive":true,"registrationDetails":null,"createdAt":[2026,7,12,22,43,28,61535700],"updatedAt":[2026,7,12,22,43,28,61535700]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"diseaseProfile":{"id":1,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PAT021","fullName":"nguyen van muoi","dateOfBirth":[2009,12,12],"gender":"MALE","phone":"0913231111","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":[2026,7,13,9,44,59,517938100],"createdAt":[2026,7,12,22,43,28,378820200],"updatedAt":[2026,7,13,9,44,59,517938100]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"nutritionRule":{"id":22,"patient":{"id":31,"account":{"id":43,"email":"domanht11@gmail.com","passwordHash":"$2a$10$XDyzP.bC4qkryUGPsYETLOw6wvPxNSvIYT1ie3yX/gMdFRw/4G6P2","role":"PATIENT","isEmailVerified":false,"isActive":true,"registrationDetails":null,"createdAt":[2026,7,12,22,43,28,61535700],"updatedAt":[2026,7,12,22,43,28,61535700]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"diseaseProfile":{"id":1,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PAT021","fullName":"nguyen van muoi","dateOfBirth":[2009,12,12],"gender":"MALE","phone":"0913231111","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":[2026,7,13,9,44,59,517938100],"createdAt":[2026,7,12,22,43,28,378820200],"updatedAt":[2026,7,13,9,44,59,517938100]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"maxCaloriesPerDay":2000,"maxCarbsG":250.00,"maxSaltG":5.00,"minFiberG":25.00,"maxFatG":7.00,"minProteinG":11.00,"dailyWaterMl":2000,"additionalNotes":"","isCurrent":true,"effectiveFrom":[2026,7,20],"createdAt":[2026,7,20,8,38,11,455658000],"updatedAt":[2026,7,20,8,38,11,455658000]},"targetSystolicBp":null,"targetDiastolicBp":null,"targetFastingGlucose":null,"targetHba1c":null,"targetWeightKg":null,"targetSteps":null,"targetSleepHours":null,"baselineSystolicBp":null,"baselineDiastolicBp":null,"baselineFastingGlucose":null,"baselineHba1c":null,"baselineWeightKg":null,"medicalOrder":"","exerciseGoal":"","additionalNotes":"","isCurrent":true,"effectiveFrom":[2026,7,20],"createdAt":[2026,7,20,8,38,11,455658000],"updatedAt":[2026,7,20,8,38,11,455658000]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 tạo/cập nhật phác đồ mới cho bệnh nhân nguyen van muoi', CAST(N'2026-07-20T08:38:11.6637569' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (29, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-20T08:38:20.7555468' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (30, N'DOCTOR', 1, N'CREATE_TREATMENT_PLAN', N'treatment_plans', 23, NULL, N'{"id":23,"patient":{"id":31,"account":{"id":43,"email":"domanht11@gmail.com","passwordHash":"$2a$10$XDyzP.bC4qkryUGPsYETLOw6wvPxNSvIYT1ie3yX/gMdFRw/4G6P2","role":"PATIENT","isEmailVerified":false,"isActive":true,"registrationDetails":null,"createdAt":[2026,7,12,22,43,28,61535700],"updatedAt":[2026,7,12,22,43,28,61535700]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"diseaseProfile":{"id":1,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PAT021","fullName":"nguyen van muoi","dateOfBirth":[2009,12,12],"gender":"MALE","phone":"0913231111","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":[2026,7,13,9,44,59,517938100],"createdAt":[2026,7,12,22,43,28,378820200],"updatedAt":[2026,7,13,9,44,59,517938100]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"nutritionRule":{"id":23,"patient":{"id":31,"account":{"id":43,"email":"domanht11@gmail.com","passwordHash":"$2a$10$XDyzP.bC4qkryUGPsYETLOw6wvPxNSvIYT1ie3yX/gMdFRw/4G6P2","role":"PATIENT","isEmailVerified":false,"isActive":true,"registrationDetails":null,"createdAt":[2026,7,12,22,43,28,61535700],"updatedAt":[2026,7,12,22,43,28,61535700]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"diseaseProfile":{"id":1,"profileCode":null,"profileName":null,"description":null,"requiresBpInput":false,"requiresGlucoseInput":false,"isActive":true},"patientCode":"PAT021","fullName":"nguyen van muoi","dateOfBirth":[2009,12,12],"gender":"MALE","phone":"0913231111","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":[2026,7,13,9,44,59,517938100],"createdAt":[2026,7,12,22,43,28,378820200],"updatedAt":[2026,7,13,9,44,59,517938100]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"maxCaloriesPerDay":2000,"maxCarbsG":250.00,"maxSaltG":5.00,"minFiberG":25.00,"maxFatG":7.00,"minProteinG":11.00,"dailyWaterMl":2000,"additionalNotes":"","isCurrent":true,"effectiveFrom":[2026,7,20],"createdAt":[2026,7,20,8,45,29,406034000],"updatedAt":[2026,7,20,8,45,29,406034000]},"targetSystolicBp":null,"targetDiastolicBp":null,"targetFastingGlucose":null,"targetHba1c":null,"targetWeightKg":null,"targetSteps":null,"targetSleepHours":null,"baselineSystolicBp":null,"baselineDiastolicBp":null,"baselineFastingGlucose":null,"baselineHba1c":null,"baselineWeightKg":null,"medicalOrder":"","exerciseGoal":"","additionalNotes":"","isCurrent":true,"effectiveFrom":[2026,7,20],"createdAt":[2026,7,20,8,45,29,406034000],"updatedAt":[2026,7,20,8,45,29,406034000]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 tạo/cập nhật phác đồ mới cho bệnh nhân nguyen van muoi', CAST(N'2026-07-20T08:45:29.6102098' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (31, N'DOCTOR', 1, N'UPDATE_PATIENT_THRESHOLD', N'AlertThreshold', 2, NULL, NULL, N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36', N'Bác sĩ Bác sĩ 1 đã cập nhật ngưỡng cảnh báo cá nhân hóa cho bệnh nhân nguyen van muoi', CAST(N'2026-07-20T08:45:34.8638083' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (32, N'DOCTOR', 1, N'PROCESS_CHANGE_REQUEST', N'change_requests', 8, NULL, N'{"id":8,"patient":{"id":1,"account":{"id":13,"email":"patient1@gmail.com","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"PATIENT","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"diseaseProfile":{"id":3,"profileCode":"BOTH","profileName":"Đồng mắc (Cả hai bệnh)","description":"Bệnh nhân bắt buộc phải nhập đầy đủ cả hai nhóm chỉ số huyết áp và glucose.","requiresBpInput":true,"requiresGlucoseInput":true,"isActive":true},"patientCode":"PAT001","fullName":"Bệnh nhân 1","dateOfBirth":null,"gender":null,"phone":"080000001","address":null,"emergencyContactName":null,"emergencyContactPhone":null,"status":"TREATING","registrationSource":"ONLINE","isActive":true,"onboardedAt":null,"createdAt":[2026,6,27,16,13,53,363404900],"updatedAt":[2026,6,27,16,13,53,363404900]},"doctor":{"id":1,"account":{"id":8,"email":"doctor1@hospital.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"DOCTOR","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,16,13,53,158809000],"updatedAt":[2026,6,27,16,13,53,158809000]},"hospital":{"id":1,"account":{"id":1,"email":"admin@bvdktrunguong-mau.vn","passwordHash":"$2a$10$Pyg54XrK4mTRsGkKD3GBuu9u5LPraBk0NdqcyiiqRW3WQjZvtmMBy","role":"HOSPITAL_ADMIN","isEmailVerified":true,"isActive":true,"registrationDetails":null,"createdAt":[2026,6,27,15,58,1,726786100],"updatedAt":[2026,6,27,15,58,1,726786100]},"hospitalCode":"BV001","fullName":"Bệnh viện Đa khoa Trung ương Mẫu","address":"123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội","phone":"024-3869-3731","isActive":true,"createdAt":[2026,6,27,15,58,1,731196300],"updatedAt":[2026,6,27,15,58,1,731196300]},"doctorCode":"DOC001","fullName":"Bác sĩ 1","phone":"0900000001","specialty":"Tiểu đường","gender":null,"capacityLimit":50,"currentPatientCount":6,"isActive":true,"createdAt":[2026,6,27,16,13,53,162351200],"updatedAt":[2026,7,13,9,44,59,519945800]},"requestType":"TREATMENT_PLAN","patientReason":"sadsadadasd","status":"REJECTED","doctorResponse":"đâs","processedAt":[2026,7,20,8,46,3,2933000],"createdAt":[2026,7,20,7,40,9,553892500],"updatedAt":[2026,7,20,8,46,3,2933000]}', N'0:0:0:0:0:0:0:1', N'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36', N'Bác sĩ REJECT yêu cầu thay đổi với ghi chú: đâs', CAST(N'2026-07-20T08:46:03.0315761' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[audit_trails] OFF
GO
SET IDENTITY_INSERT [dbo].[change_requests] ON 
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (1, 1, 1, N'TREATMENT_PLAN', N't không thích', N'APPROVED', N'ok', CAST(N'2026-06-28T22:27:32.9460448' AS DateTime2), CAST(N'2026-06-28T22:25:32.4699294' AS DateTime2), CAST(N'2026-06-28T22:27:32.9460448' AS DateTime2))
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (2, 1, 1, N'TREATMENT_PLAN', N'ádasds', N'REJECTED', N'kệ m?', CAST(N'2026-06-28T22:27:21.0818727' AS DateTime2), CAST(N'2026-06-28T22:26:16.3900686' AS DateTime2), CAST(N'2026-06-28T22:27:21.0818727' AS DateTime2))
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (3, 1, 1, N'SCHEDULE', N'ddddd', N'REJECTED', N'ngu', CAST(N'2026-06-29T08:00:46.7362023' AS DateTime2), CAST(N'2026-06-29T07:59:48.9688864' AS DateTime2), CAST(N'2026-06-29T08:00:46.7362023' AS DateTime2))
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (4, 1, 1, N'SCHEDULE', N'đâs', N'REJECTED', N'dsad', CAST(N'2026-07-10T16:08:55.0600772' AS DateTime2), CAST(N'2026-06-29T09:07:56.4553355' AS DateTime2), CAST(N'2026-07-10T16:08:55.0600772' AS DateTime2))
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (5, 1, 1, N'SCHEDULE', N'đâs', N'REJECTED', N'dsadsa', CAST(N'2026-07-10T16:08:57.7776784' AS DateTime2), CAST(N'2026-06-29T09:08:06.9578974' AS DateTime2), CAST(N'2026-07-10T16:08:57.7776784' AS DateTime2))
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (6, 1, 1, N'TREATMENT_PLAN', N'dsadasdasdadsadadsaadasqqqqqqqqqqqqqqqqqqqqq', N'PENDING', NULL, NULL, CAST(N'2026-07-13T09:16:11.5314746' AS DateTime2), CAST(N'2026-07-13T09:16:11.5314746' AS DateTime2))
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (7, 1, 1, N'TREATMENT_PLAN', N'dsadasdasdadsadadsaadasqqqqqqqqqqqqqqqqqqqqq', N'REJECTED', N'dsadsa', CAST(N'2026-07-13T10:10:34.1433114' AS DateTime2), CAST(N'2026-07-13T09:16:24.9638486' AS DateTime2), CAST(N'2026-07-13T10:10:34.1433114' AS DateTime2))
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (8, 1, 1, N'TREATMENT_PLAN', N'sadsadadasd', N'REJECTED', N'đâs', CAST(N'2026-07-20T08:46:03.0029330' AS DateTime2), CAST(N'2026-07-20T07:40:09.5538925' AS DateTime2), CAST(N'2026-07-20T08:46:03.0029330' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[change_requests] OFF
GO
SET IDENTITY_INSERT [dbo].[daily_health_logs] ON 
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (8, 1, CAST(N'2026-06-20' AS Date), CAST(N'2026-06-20T08:00:00.0000000' AS DateTime2), N'MORNING', 127, 82, 75, CAST(6.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:28:29.8566667' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (9, 1, CAST(N'2026-06-21' AS Date), CAST(N'2026-06-21T08:00:00.0000000' AS DateTime2), N'MORNING', 126, 81, 75, CAST(5.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:28:29.8600000' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (10, 1, CAST(N'2026-06-22' AS Date), CAST(N'2026-06-22T08:00:00.0000000' AS DateTime2), N'MORNING', 125, 80, 75, CAST(7.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:28:29.8600000' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (11, 1, CAST(N'2026-06-23' AS Date), CAST(N'2026-06-23T08:00:00.0000000' AS DateTime2), N'MORNING', 124, 84, 75, CAST(6.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:28:29.8600000' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (12, 1, CAST(N'2026-06-24' AS Date), CAST(N'2026-06-24T08:00:00.0000000' AS DateTime2), N'MORNING', 123, 83, 75, CAST(5.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:28:29.8600000' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (13, 1, CAST(N'2026-06-25' AS Date), CAST(N'2026-06-25T08:00:00.0000000' AS DateTime2), N'MORNING', 122, 82, 75, CAST(7.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:28:29.8600000' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (14, 1, CAST(N'2026-06-26' AS Date), CAST(N'2026-06-26T08:00:00.0000000' AS DateTime2), N'MORNING', 121, 81, 75, CAST(6.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:28:29.8633333' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (15, 2, CAST(N'2026-06-20' AS Date), CAST(N'2026-06-20T08:00:00.0000000' AS DateTime2), N'MORNING', 127, 82, 75, CAST(6.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:29:13.3700000' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (16, 2, CAST(N'2026-06-21' AS Date), CAST(N'2026-06-21T08:00:00.0000000' AS DateTime2), N'MORNING', 126, 81, 75, CAST(5.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:29:13.3700000' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (17, 2, CAST(N'2026-06-22' AS Date), CAST(N'2026-06-22T08:00:00.0000000' AS DateTime2), N'MORNING', 125, 80, 75, CAST(7.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:29:13.3700000' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (18, 2, CAST(N'2026-06-23' AS Date), CAST(N'2026-06-23T08:00:00.0000000' AS DateTime2), N'MORNING', 124, 84, 75, CAST(6.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:29:13.3733333' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (19, 2, CAST(N'2026-06-24' AS Date), CAST(N'2026-06-24T08:00:00.0000000' AS DateTime2), N'MORNING', 123, 83, 75, CAST(5.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:29:13.3733333' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (20, 2, CAST(N'2026-06-25' AS Date), CAST(N'2026-06-25T08:00:00.0000000' AS DateTime2), N'MORNING', 122, 82, 75, CAST(7.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:29:13.3733333' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (21, 2, CAST(N'2026-06-26' AS Date), CAST(N'2026-06-26T08:00:00.0000000' AS DateTime2), N'MORNING', 121, 81, 75, CAST(6.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-27T22:29:13.3733333' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (27, 30, CAST(N'2026-07-07' AS Date), CAST(N'2026-07-07T15:14:10.2989295' AS DateTime2), N'RANDOM', 80, 30, NULL, CAST(10.00 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Khám định kỳ/nhập viện. Bác sĩ Bác sĩ 1 ghi nhận.', 0, NULL, NULL, CAST(N'2026-07-07T15:14:10.3466684' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (28, 2, CAST(N'2026-07-07' AS Date), CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2), N'RANDOM', 100, 30, NULL, CAST(20.00 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Khám định kỳ/nhập viện. Bác sĩ Bác sĩ 1 ghi nhận.', 0, NULL, NULL, CAST(N'2026-07-07T15:23:45.6068507' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (29, 1, CAST(N'2026-07-07' AS Date), CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2), N'RANDOM', 120, 40, NULL, CAST(20.00 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Khám định kỳ/nhập viện. Bác sĩ Bác sĩ 1 ghi nhận.', 0, NULL, NULL, CAST(N'2026-07-07T15:27:41.1000932' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (30, 30, CAST(N'2026-07-10' AS Date), CAST(N'2026-07-10T21:28:51.7382347' AS DateTime2), N'RANDOM', 80, 30, NULL, CAST(10.00 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Khám định kỳ/nhập viện. Bác sĩ Bác sĩ 1 ghi nhận.', 0, NULL, NULL, CAST(N'2026-07-10T21:28:51.8529703' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[daily_health_logs] OFF
GO
SET IDENTITY_INSERT [dbo].[disease_profiles] ON 
GO
INSERT [dbo].[disease_profiles] ([id], [profile_code], [profile_name], [description], [requires_bp_input], [requires_glucose_input], [is_active]) VALUES (1, N'HYPERTENSION', N'Tăng huyết áp', N'Bệnh nhân chỉ theo dõi huyết áp và nhịp tim. Màn hình ẩn phần nhập tiểu đường.', 1, 0, 1)
GO
INSERT [dbo].[disease_profiles] ([id], [profile_code], [profile_name], [description], [requires_bp_input], [requires_glucose_input], [is_active]) VALUES (2, N'DIABETES', N'Tiểu đường Type 2', N'Bệnh nhân chỉ theo dõi glucose. Màn hình ẩn phần nhập huyết áp.', 0, 1, 1)
GO
INSERT [dbo].[disease_profiles] ([id], [profile_code], [profile_name], [description], [requires_bp_input], [requires_glucose_input], [is_active]) VALUES (3, N'BOTH', N'Đồng mắc (Cả hai bệnh)', N'Bệnh nhân bắt buộc phải nhập đầy đủ cả hai nhóm chỉ số huyết áp và glucose.', 1, 1, 1)
GO
SET IDENTITY_INSERT [dbo].[disease_profiles] OFF
GO
SET IDENTITY_INSERT [dbo].[doctors] ON 
GO
INSERT [dbo].[doctors] ([id], [account_id], [hospital_id], [doctor_code], [full_name], [gender], [phone], [specialty], [capacity_limit], [current_patient_count], [is_active], [created_at], [updated_at]) VALUES (1, 8, 1, N'DOC001', N'Bác sĩ 1', NULL, N'0900000001', N'Tiểu đường', 50, 6, 1, CAST(N'2026-06-27T16:13:53.1623512' AS DateTime2), CAST(N'2026-07-13T09:44:59.5199458' AS DateTime2))
GO
INSERT [dbo].[doctors] ([id], [account_id], [hospital_id], [doctor_code], [full_name], [gender], [phone], [specialty], [capacity_limit], [current_patient_count], [is_active], [created_at], [updated_at]) VALUES (2, 9, 1, N'DOC002', N'Bác sĩ 2', NULL, N'0900000002', N'Tiểu đường', 50, 4, 1, CAST(N'2026-06-27T16:13:53.3593845' AS DateTime2), CAST(N'2026-06-27T16:13:53.3593845' AS DateTime2))
GO
INSERT [dbo].[doctors] ([id], [account_id], [hospital_id], [doctor_code], [full_name], [gender], [phone], [specialty], [capacity_limit], [current_patient_count], [is_active], [created_at], [updated_at]) VALUES (3, 10, 1, N'DOC003', N'Bác sĩ 3', NULL, N'0900000003', N'Tiểu đường', 50, 4, 1, CAST(N'2026-06-27T16:13:53.3614087' AS DateTime2), CAST(N'2026-06-27T16:13:53.3614087' AS DateTime2))
GO
INSERT [dbo].[doctors] ([id], [account_id], [hospital_id], [doctor_code], [full_name], [gender], [phone], [specialty], [capacity_limit], [current_patient_count], [is_active], [created_at], [updated_at]) VALUES (4, 11, 1, N'DOC004', N'Bác sĩ 4', NULL, N'0900000004', N'Tiểu đường', 50, 4, 1, CAST(N'2026-06-27T16:13:53.3624004' AS DateTime2), CAST(N'2026-06-27T16:13:53.3624004' AS DateTime2))
GO
INSERT [dbo].[doctors] ([id], [account_id], [hospital_id], [doctor_code], [full_name], [gender], [phone], [specialty], [capacity_limit], [current_patient_count], [is_active], [created_at], [updated_at]) VALUES (5, 12, 1, N'DOC005', N'Bác sĩ 5', NULL, N'0900000005', N'Tiểu đường', 50, 4, 1, CAST(N'2026-06-27T16:13:53.3624004' AS DateTime2), CAST(N'2026-06-27T16:13:53.3624004' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[doctors] OFF
GO
SET IDENTITY_INSERT [dbo].[emergency_guides] ON 
GO
INSERT [dbo].[emergency_guides] ([id], [hospital_id], [alert_level], [metric_type], [title], [instruction_content], [is_active], [created_at], [updated_at]) VALUES (1, 1, N'RED', N'BLOOD_PRESSURE', N'Huyết áp tăng cao — Hướng dẫn xử lý', N'1. Ngừng ngay mọi hoạt động thể chất, nằm xuống nghỉ ngơi ở tư thế thoải mái.
2. Thở sâu và chậm, hít vào 4 giây, giữ 2 giây, thở ra 6 giây. Lặp lại 5-10 lần.
3. Tránh hoàn toàn cafein, muối và các chất kích thích.
4. Uống thuốc huyết áp theo đúng chỉ định của bác sĩ nếu đã đến giờ uống.
5. Đo lại sau 15-20 phút. Nếu không giảm, liên hệ ngay người thân hoặc gọi cấp cứu 115.', 1, CAST(N'2026-06-27T15:58:01.7455993' AS DateTime2), CAST(N'2026-06-27T15:58:01.7455993' AS DateTime2))
GO
INSERT [dbo].[emergency_guides] ([id], [hospital_id], [alert_level], [metric_type], [title], [instruction_content], [is_active], [created_at], [updated_at]) VALUES (2, 1, N'RED', N'GLUCOSE', N'Đường huyết vượt ngưỡng — Hướng dẫn xử lý', N'1. Không ăn thêm bất kỳ thực phẩm có đường hoặc tinh bột nào.
2. Uống nước lọc (không đường) từ từ, khoảng 200-300ml.
3. Nghỉ ngơi hoàn toàn, tránh vận động mạnh ngay lúc này.
4. Kiểm tra lại đã uống thuốc tiểu đường đúng giờ chưa.
5. Đo lại đường huyết sau 1 tiếng. Nếu vẫn cao, liên hệ bác sĩ ngay.', 1, CAST(N'2026-06-27T15:58:01.7455993' AS DateTime2), CAST(N'2026-06-27T15:58:01.7455993' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[emergency_guides] OFF
GO
SET IDENTITY_INSERT [dbo].[emergency_protocols] ON 
GO
INSERT [dbo].[emergency_protocols] ([id], [hospital_id], [condition_type], [title], [warning_signs], [instruction_content], [is_active], [created_at], [updated_at]) VALUES (1, 1, N'HYPERTENSIVE_CRISIS', N'Tăng huyết áp đột ngột (Cơn tăng huyết áp)', N'Đau đầu dữ dội, chóng mặt, mờ mắt, đau ngực, khó thở, tê yếu tay chân, lú lẫn, chảy máu mũi bất thường.', N'1. Cho người bệnh ngồi/nằm nghỉ ở tư thế đầu cao 30-45 độ, giữ bình tĩnh.
2. Đo lại huyết áp sau 5 phút để xác nhận.
3. Nếu huyết áp tâm thu >=180 hoặc tâm trương >=110 mmHg: gọi 115 hoặc đưa đến cơ sở y tế gần nhất ngay.
4. Không tự ý dùng thêm thuốc hạ áp ngoài chỉ định của bác sĩ.', 1, CAST(N'2026-06-27T15:58:01.7516118' AS DateTime2), CAST(N'2026-06-27T15:58:01.7516118' AS DateTime2))
GO
INSERT [dbo].[emergency_protocols] ([id], [hospital_id], [condition_type], [title], [warning_signs], [instruction_content], [is_active], [created_at], [updated_at]) VALUES (2, 1, N'HYPOGLYCEMIA', N'Hạ đường huyết đột ngột', N'Vã mồ hôi lạnh, run tay chân, hoa mắt, chóng mặt, đói cồn cào, tim đập nhanh, lú lẫn, có thể ngất xỉu.', N'1. Cho người bệnh ăn/uống ngay 15g đường nhanh (nửa cốc nước ngọt, vài viên kẹo, hoặc 3 thìa đường pha nước).
2. Nghỉ ngơi, đo lại đường huyết sau 15 phút.
3. Nếu vẫn dưới 4.4 mmol/L hoặc người bệnh lơ mơ/mất ý thức: gọi 115 ngay, không cố ép ăn uống khi đã mất ý thức.', 1, CAST(N'2026-06-27T15:58:01.7516118' AS DateTime2), CAST(N'2026-06-27T15:58:01.7516118' AS DateTime2))
GO
INSERT [dbo].[emergency_protocols] ([id], [hospital_id], [condition_type], [title], [warning_signs], [instruction_content], [is_active], [created_at], [updated_at]) VALUES (3, 1, N'HYPERGLYCEMIA', N'Tăng đường huyết đột ngột', N'Khát nước nhiều, đi tiểu nhiều, mệt mỏi, mờ mắt, hơi thở có mùi trái cây (dấu hiệu nhiễm toan ceton), buồn nôn.', N'1. Uống nhiều nước lọc, không ăn thêm tinh bột/đường.
2. Kiểm tra lại đã dùng thuốc/insulin đúng giờ chưa.
3. Đo lại đường huyết sau 1 giờ.
4. Nếu đường huyết > 16 mmol/L kèm mệt nhiều, nôn ói, thở nhanh: liên hệ bác sĩ hoặc đến cơ sở y tế ngay.', 1, CAST(N'2026-06-27T15:58:01.7516118' AS DateTime2), CAST(N'2026-06-27T15:58:01.7516118' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[emergency_protocols] OFF
GO
SET IDENTITY_INSERT [dbo].[exercise_logs] ON 
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (8, 1, CAST(N'2026-06-20' AS Date), N'Đi bộ', 65, 6500, CAST(N'2026-06-27T22:28:29.8600000' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (9, 1, CAST(N'2026-06-21' AS Date), N'Đi bộ', 60, 6000, CAST(N'2026-06-27T22:28:29.8600000' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (10, 1, CAST(N'2026-06-22' AS Date), N'Đi bộ', 55, 5500, CAST(N'2026-06-27T22:28:29.8600000' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (11, 1, CAST(N'2026-06-23' AS Date), N'Đi bộ', 50, 5000, CAST(N'2026-06-27T22:28:29.8600000' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (12, 1, CAST(N'2026-06-24' AS Date), N'Đi bộ', 45, 4500, CAST(N'2026-06-27T22:28:29.8600000' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (13, 1, CAST(N'2026-06-25' AS Date), N'Đi bộ', 40, 4000, CAST(N'2026-06-27T22:28:29.8600000' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (14, 1, CAST(N'2026-06-26' AS Date), N'Đi bộ', 35, 3500, CAST(N'2026-06-27T22:28:29.8633333' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (15, 2, CAST(N'2026-06-20' AS Date), N'Đi bộ', 65, 6500, CAST(N'2026-06-27T22:29:13.3700000' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (16, 2, CAST(N'2026-06-21' AS Date), N'Đi bộ', 60, 6000, CAST(N'2026-06-27T22:29:13.3700000' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (17, 2, CAST(N'2026-06-22' AS Date), N'Đi bộ', 55, 5500, CAST(N'2026-06-27T22:29:13.3700000' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (18, 2, CAST(N'2026-06-23' AS Date), N'Đi bộ', 50, 5000, CAST(N'2026-06-27T22:29:13.3733333' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (19, 2, CAST(N'2026-06-24' AS Date), N'Đi bộ', 45, 4500, CAST(N'2026-06-27T22:29:13.3733333' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (20, 2, CAST(N'2026-06-25' AS Date), N'Đi bộ', 40, 4000, CAST(N'2026-06-27T22:29:13.3733333' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (21, 2, CAST(N'2026-06-26' AS Date), N'Đi bộ', 35, 3500, CAST(N'2026-06-27T22:29:13.3733333' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (22, 1, CAST(N'2026-07-07' AS Date), N'Đi bộ', 30, 3000, CAST(N'2026-07-07T15:29:42.4228608' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (23, 1, CAST(N'2026-07-07' AS Date), N'Đạp xe', 30, NULL, CAST(N'2026-07-07T15:29:50.7360974' AS DateTime2), NULL)
GO
SET IDENTITY_INSERT [dbo].[exercise_logs] OFF
GO
SET IDENTITY_INSERT [dbo].[foods_dictionary] ON 
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (1, N'V01', N'Cơm trắng', N'White rice (cooked)', CAST(68.70 AS Decimal(5, 2)), 130, CAST(2.70 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(28.20 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-27T15:58:01.7591736' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (2, N'V02', N'Rau muống luộc', N'Boiled water spinach', CAST(91.00 AS Decimal(5, 2)), 23, CAST(2.60 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-27T15:58:01.7591736' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (3, N'A01', N'Thịt gà luộc (bỏ da)', N'Boiled chicken breast (skinless)', CAST(65.00 AS Decimal(5, 2)), 165, CAST(31.00 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), CAST(0.00 AS Decimal(5, 2)), CAST(0.00 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-27T15:58:01.7591736' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (4, N'A02', N'Cá hồi áp chảo', N'Pan-seared salmon', CAST(64.00 AS Decimal(5, 2)), 208, CAST(20.40 AS Decimal(5, 2)), CAST(13.40 AS Decimal(5, 2)), CAST(0.00 AS Decimal(5, 2)), CAST(0.00 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-27T15:58:01.7591736' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[foods_dictionary] OFF
GO
SET IDENTITY_INSERT [dbo].[hospitals] ON 
GO
INSERT [dbo].[hospitals] ([id], [account_id], [hospital_code], [full_name], [address], [phone], [is_active], [created_at], [updated_at]) VALUES (1, 1, N'BV001', N'Bệnh viện Đa khoa Trung ương Mẫu', N'123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội', N'024-3869-3731', 1, CAST(N'2026-06-27T15:58:01.7311963' AS DateTime2), CAST(N'2026-06-27T15:58:01.7311963' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[hospitals] OFF
GO
SET IDENTITY_INSERT [dbo].[medication_logs] ON 
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (8, 2, CAST(N'2026-06-20' AS Date), 1, CAST(N'2026-06-20T08:00:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (9, 2, CAST(N'2026-06-21' AS Date), 0, NULL)
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (10, 2, CAST(N'2026-06-22' AS Date), 1, CAST(N'2026-06-22T08:00:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (11, 2, CAST(N'2026-06-23' AS Date), 1, CAST(N'2026-06-23T08:00:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (12, 2, CAST(N'2026-06-24' AS Date), 0, NULL)
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (13, 2, CAST(N'2026-06-25' AS Date), 1, CAST(N'2026-06-25T08:00:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (14, 2, CAST(N'2026-06-26' AS Date), 1, CAST(N'2026-06-26T08:00:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (15, 3, CAST(N'2026-06-20' AS Date), 1, CAST(N'2026-06-20T08:00:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (16, 3, CAST(N'2026-06-21' AS Date), 0, NULL)
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (17, 3, CAST(N'2026-06-22' AS Date), 1, CAST(N'2026-06-22T08:00:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (18, 3, CAST(N'2026-06-23' AS Date), 1, CAST(N'2026-06-23T08:00:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (19, 3, CAST(N'2026-06-24' AS Date), 0, NULL)
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (20, 3, CAST(N'2026-06-25' AS Date), 1, CAST(N'2026-06-25T08:00:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (21, 3, CAST(N'2026-06-26' AS Date), 1, CAST(N'2026-06-26T08:00:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (22, 10, CAST(N'2026-07-02' AS Date), 0, NULL)
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (23, 10, CAST(N'2026-07-07' AS Date), 1, CAST(N'2026-07-07T15:26:12.7228535' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (25, 15, CAST(N'2026-07-07' AS Date), 1, CAST(N'2026-07-07T15:29:23.5379562' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (24, 16, CAST(N'2026-07-07' AS Date), 1, CAST(N'2026-07-07T15:29:22.4025986' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[medication_logs] OFF
GO
SET IDENTITY_INSERT [dbo].[notifications] ON 
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (3, N'DOCTOR', 1, N'SYSTEM', N'Cảnh báo sức khỏe bệnh nhân', N'B?nh nhân có s? ki?n b?t thư?ng c?n ki?m tra ngay l?p t?c.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-06-27T22:29:13.3766667' AS DateTime2), 2, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (8, N'PATIENT', 2, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác s? Bác s? 1 v?a c?p nh?t phác đ? đi?u tr? & danh m?c thu?c c?a b?n. Vui l?ng ki?m tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-28T23:23:16.6019787' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (9, N'PATIENT', 1, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác s? Bác s? 1 v?a c?p nh?t phác đ? đi?u tr? & danh m?c thu?c c?a b?n. Vui l?ng ki?m tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-28T23:29:42.6118036' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (10, N'PATIENT', 2, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác s? Bác s? 1 v?a c?p nh?t phác đ? đi?u tr? & danh m?c thu?c c?a b?n. Vui l?ng ki?m tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-28T23:39:35.4039638' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (11, N'PATIENT', 2, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác s? Bác s? 1 v?a c?p nh?t phác đ? đi?u tr? & danh m?c thu?c c?a b?n. Vui l?ng ki?m tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-28T23:49:50.3835037' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (12, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác s? Bác s? 1 đ? x?p l?ch tái khám vào 05/07/2026 23:54 t?i dsadsa232.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-28T23:54:59.6213620' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (13, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác s? Bác s? 1 đ? x?p l?ch tái khám vào 29/06/2026 00:00 t?i adsda.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-28T23:56:03.2380089' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (14, N'PATIENT', 2, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác s? Bác s? 1 v?a c?p nh?t phác đ? đi?u tr? & danh m?c thu?c c?a b?n. Vui l?ng ki?m tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-28T23:56:22.6811631' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (15, N'PATIENT', 2, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác s? Bác s? 1 v?a c?p nh?t phác đ? đi?u tr? & danh m?c thu?c c?a b?n. Vui l?ng ki?m tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-28T23:58:40.2736029' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (16, N'PATIENT', 1, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác s? Bác s? 1 v?a c?p nh?t phác đ? đi?u tr? & danh m?c thu?c c?a b?n. Vui l?ng ki?m tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-29T07:57:24.5070188' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (17, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_10', N'Nhắc nhở uống thuốc sắp tới', N'S?p đ?n gi? u?ng thu?c: Metformin 500mg (1 viên) lúc 08:00.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-06-29T07:57:47.9584881' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (19, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_10', N'Nhắc nhở quá giờ uống thuốc', N'Đ? quá gi? h?n u?ng thu?c: Metformin 500mg (1 viên) lúc 08:00 nhưng b?n chưa ghi nh?n k?t qu?.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-29T08:00:06.1743543' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (20, N'PATIENT', 0, N'SYSTEM', N'Phản hồi yêu cầu đổi phác đồ', N'Bác s? đ? t? ch?i yêu c?u c?a b?n: ngu', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-29T08:00:46.7503138' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (21, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác s? Bác s? 1 đ? x?p l?ch tái khám vào 01/07/2026 00:05 t?i dsadsa232.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-29T09:06:53.5130493' AS DateTime2), 2, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (24, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác s? Bác s? 1 đ? x?p l?ch tái khám vào 30/06/2026 09:21 t?i dsadsadsa.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-29T09:21:38.6984400' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (25, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác s? Bác s? 1 đ? x?p l?ch tái khám vào 01/07/2026 09:21 t?i đâsdsad.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-29T09:21:47.9258144' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (26, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác s? Bác s? 1 đ? x?p l?ch tái khám vào 08/07/2026 11:21 t?i đâsdasdas.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-06-29T09:21:57.6821609' AS DateTime2), 2, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (28, N'PATIENT', 1, N'HEALTH_LOG_REMINDER', N'Nhắc nhở nhập chỉ số sức khỏe', N'Đ? quá 20:00 nhưng b?n chưa nh?p ch? s? s?c kh?e (huy?t áp, đư?ng huy?t...) cho hôm nay. Vui l?ng ghi nh?n k?t qu?.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-02T22:31:36.2216063' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (29, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_10', N'Nhắc nhở quá giờ uống thuốc', N'Đ? quá gi? h?n u?ng thu?c: Metformin 500mg (1 viên) lúc 08:00 nhưng b?n chưa ghi nh?n k?t qu?.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-02T22:32:28.1664491' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (30, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác s? Bác s? 1 đ? x?p l?ch tái khám vào 03/07/2026 23:03 t?i Ph?ng khám Test T? Đ?ng 102.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-02T23:03:05.6983335' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (31, N'PATIENT', 2, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác s? Bác s? 1 v?a c?p nh?t phác đ? đi?u tr? & danh m?c thu?c c?a b?n. Vui l?ng ki?m tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-03T09:55:14.5887075' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (32, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác s? Bác s? 1 đ? x?p l?ch tái khám vào 04/07/2026 10:08 t?i Ph?ng khám Test T? Đ?ng 102.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-03T10:08:34.9500080' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (33, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác s? Bác s? 1 đ? x?p l?ch tái khám vào 04/07/2026 10:10 t?i Ph?ng khám Test T? Đ?ng 102.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-03T10:10:52.5060635' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (34, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_10', N'Nhắc nhở quá giờ uống thuốc', N'Đ? quá gi? h?n u?ng thu?c: Metformin 500mg (1 viên) lúc 08:00 nhưng b?n chưa ghi nh?n k?t qu?.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-03T10:28:18.0352263' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (35, N'PATIENT', 30, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác s? Bác s? 1 v?a c?p nh?t phác đ? đi?u tr? & danh m?c thu?c c?a b?n. Vui l?ng ki?m tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-07T15:14:10.2989295' AS DateTime2), 30, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (36, N'PATIENT', 2, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác s? Bác s? 1 v?a c?p nh?t phác đ? đi?u tr? & danh m?c thu?c c?a b?n. Vui l?ng ki?m tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (38, N'PATIENT', 1, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác s? Bác s? 1 v?a c?p nh?t phác đ? đi?u tr? & danh m?c thu?c c?a b?n. Vui l?ng ki?m tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (41, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác sĩ Bác sĩ 1 đã xếp lịch tái khám vào 11/07/2026 16:07 tại sdsdasd.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-10T16:08:10.8789845' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (42, N'PATIENT', 0, N'SYSTEM', N'Phản hồi yêu cầu đổi phác đồ', N'Bác sĩ đã từ chối yêu cầu của bạn: dsad', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-10T16:08:55.0789230' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (43, N'PATIENT', 0, N'SYSTEM', N'Phản hồi yêu cầu đổi phác đồ', N'Bác sĩ đã từ chối yêu cầu của bạn: dsadsa', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-10T16:08:57.7889661' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (44, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác sĩ Bác sĩ 1 đã xếp lịch tái khám vào 19/07/2026 16:23 tại dsdsadsa.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-10T16:24:03.3756269' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (45, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác sĩ Bác sĩ 1 đã xếp lịch tái khám vào 17/07/2026 16:24 tại sadasd.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-10T16:24:29.8042285' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (46, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác sĩ Bác sĩ 1 đã xếp lịch tái khám vào 25/07/2026 16:43 tại dsad.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-10T16:43:44.7605658' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (47, N'PATIENT', 30, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác sĩ Bác sĩ 1 vừa cập nhật phác đồ điều trị & danh mục thuốc của bạn. Vui lòng kiểm tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-10T21:28:51.7382347' AS DateTime2), 30, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (48, N'PATIENT', 32, N'HEALTH_LOG_REMINDER', N'Nhắc nhở nhập chỉ số sức khỏe', N'Đã quá 20:00 nhưng bạn chưa nhập chỉ số sức khỏe (huyết áp, đường huyết...) cho hôm nay. Vui lòng ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T22:45:59.3144662' AS DateTime2), 32, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (49, N'PATIENT', 32, N'EXERCISE_REMINDER', N'Nhắc nhở tập luyện', N'Bạn chưa ghi nhận vận động hôm nay. Hãy dành ít phút hoạt động để đạt mục tiêu sức khỏe nhé!', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T22:45:59.3369078' AS DateTime2), 32, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (50, N'PATIENT', 32, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T22:45:59.3698638' AS DateTime2), 32, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (51, N'PATIENT', 32, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa trưa hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T22:45:59.3818559' AS DateTime2), 32, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (52, N'PATIENT', 32, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa tối hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T22:45:59.3853271' AS DateTime2), 32, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (53, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_15', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: Metformin 500mg (1 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T22:53:13.2630283' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (54, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_16', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: dsadsadsa (đâsdsadasdsad) lúc 09:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T22:53:13.2855315' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (55, N'PATIENT', 1, N'HEALTH_LOG_REMINDER', N'Nhắc nhở nhập chỉ số sức khỏe', N'Đã quá 20:00 nhưng bạn chưa nhập chỉ số sức khỏe (huyết áp, đường huyết...) cho hôm nay. Vui lòng ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T22:53:13.3056530' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (56, N'PATIENT', 1, N'EXERCISE_REMINDER', N'Nhắc nhở tập luyện', N'Bạn chưa ghi nhận vận động hôm nay. Hãy dành ít phút hoạt động để đạt mục tiêu sức khỏe nhé!', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-12T22:53:13.3214252' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (57, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-12T22:53:13.3421744' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (58, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa trưa hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-12T22:53:13.3461807' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (59, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa tối hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-12T22:53:13.3547942' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (60, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác sĩ Bác sĩ 1 đã xếp lịch tái khám vào 30/07/2026 09:00 tại đá.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T09:00:49.5462565' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (61, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_15', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: Metformin 500mg (1 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T09:01:24.2490176' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (62, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_16', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: dsadsadsa (đâsdsadasdsad) lúc 09:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T09:01:24.2858891' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (63, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T09:01:24.3525980' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (64, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã bị từ chối', N'Bác sĩ Bác sĩ 1 đã từ chối lịch khám vào 14/07/2026 09:01. Lý do: k rah', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T09:02:54.7612540' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (67, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 15/07/2026 09:24. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-13T09:24:56.6191519' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (68, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 15/07/2026 09:24.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T09:25:27.6483544' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (69, N'PATIENT', 0, N'SYSTEM', N'Phản hồi yêu cầu đổi phác đồ', N'Bác sĩ đã từ chối yêu cầu của bạn: dsadsa', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T10:10:34.1547678' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (70, N'PATIENT', 1, N'HEALTH_LOG_REMINDER', N'Nhắc nhở nhập chỉ số sức khỏe', N'Đã quá 20:00 nhưng bạn chưa nhập chỉ số sức khỏe (huyết áp, đường huyết...) cho hôm nay. Vui lòng ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T23:13:16.2374161' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (71, N'PATIENT', 1, N'EXERCISE_REMINDER', N'Nhắc nhở tập luyện', N'Bạn chưa ghi nhận vận động hôm nay. Hãy dành ít phút hoạt động để đạt mục tiêu sức khỏe nhé!', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T23:13:16.4117259' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (72, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa trưa hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T23:13:16.4698564' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (73, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa tối hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T23:13:16.4858434' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (74, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 15/07/2026 09:13. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-13T23:14:03.0581459' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (75, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 16/07/2026 11:14. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-13T23:14:15.5245396' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (76, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 17/07/2026 11:14. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-13T23:14:25.0411147' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (77, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 21/07/2026 11:14. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-13T23:14:58.3176067' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (78, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 22/07/2026 11:15. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-13T23:15:57.4716768' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (79, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 15/07/2026 09:13.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T23:17:16.8884086' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (80, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 16/07/2026 11:14.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T23:18:30.2165927' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (81, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã bị từ chối', N'Bác sĩ Bác sĩ 1 đã từ chối lịch khám vào 17/07/2026 11:14. Lý do: Tìm kiếm trên Facebook
Menu trên Facebook
Lối tắt của bạn
Quyền riêng tư  · Điều khoản  · Quảng cáo  · Lựa chọn quảng cáo   · Cookie  · 
Trang chủ
Tạo bài viết
Tin
Bài viết trên Bảng feed
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Facebook
Thanh lý vợt', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T23:33:01.5890167' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (82, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 14/07/2026 08:13. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:13:21.1987405' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (83, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 15/07/2026 08:16. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:13:35.4898313' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (84, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 14/07/2026 08:13.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:13:47.5774187' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (85, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 15/07/2026 08:16.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:14:20.7703085' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (86, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 14/07/2026 08:29. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:29:36.4933134' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (87, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 15/07/2026 08:29. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:29:45.3904236' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (88, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 16/07/2026 08:29. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:29:53.7081438' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (89, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 17/07/2026 08:29. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:30:03.2235009' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (90, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 14/07/2026 09:30. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:30:37.5731981' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (91, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 14/07/2026 10:30. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:30:51.3235504' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (92, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 14/07/2026 08:29.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:31:43.8073520' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (93, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 14/07/2026 09:30.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:32:17.3663537' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (94, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 14/07/2026 10:30.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:32:32.4658582' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (95, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 15/07/2026 08:29.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:42:54.0342456' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (96, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 16/07/2026 08:29.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:43:27.5418395' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (97, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 17/07/2026 08:29.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:43:42.5044876' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (98, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã bị từ chối', N'Bác sĩ Bác sĩ 1 đã từ chối lịch khám vào 21/07/2026 11:14. Lý do: Doctor has an unexpected surgery', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:43:46.0378762' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (99, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã bị từ chối', N'Bác sĩ Bác sĩ 1 đã từ chối lịch khám vào 22/07/2026 11:15. Lý do: aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:43:52.9804518' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (100, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 14/07/2026 08:52. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:52:34.9154396' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (101, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 15/07/2026 08:52. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:52:45.1530387' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (102, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 16/07/2026 08:52. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:52:52.8041812' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (103, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 17/07/2026 08:52. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:53:02.1072557' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (104, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 15/07/2026 09:53. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:53:28.3822446' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (105, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 14/07/2026 10:53. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-14T01:53:38.6152818' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (106, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 14/07/2026 08:52.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:54:26.3262349' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (107, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 14/07/2026 10:53.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:54:59.7853103' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (108, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã được chấp nhận', N'Bác sĩ Bác sĩ 1 đã xác nhận lịch khám vào 15/07/2026 08:52.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:55:04.6731932' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (109, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã bị từ chối', N'Bác sĩ Bác sĩ 1 đã từ chối lịch khám vào 15/07/2026 09:53. Lý do: Doctor has an unexpected surgery', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:55:07.9601196' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (110, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã bị từ chối', N'Bác sĩ Bác sĩ 1 đã từ chối lịch khám vào 16/07/2026 08:52. Lý do: aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:55:14.3739494' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (111, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã bị từ chối', N'Bác sĩ Bác sĩ 1 đã từ chối lịch khám vào 17/07/2026 08:52. Lý do: Doctor has an unexpected surgery - 1783968943393', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T01:55:43.8220521' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (112, N'PATIENT', 0, N'SYSTEM', N'Lịch khám đã bị từ chối', N'Bác sĩ Bác sĩ 1 đã từ chối lịch khám vào 02/07/2026 09:58. Lý do: ưdasds', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-16T09:06:55.1096124' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (113, N'PATIENT', 31, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác sĩ Bác sĩ 1 vừa cập nhật phác đồ điều trị & danh mục thuốc của bạn. Vui lòng kiểm tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-18T14:22:47.5313628' AS DateTime2), 31, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (114, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác sĩ Bác sĩ 1 đã xếp lịch tái khám vào 20/07/2026 10:38 tại adsda.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T07:39:10.7433266' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (115, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_15', N'Nhắc nhở uống thuốc sắp tới', N'Bạn có lịch hẹn uống thuốc: Metformin 500mg (1 viên) lúc 08:00. Vui lòng chuẩn bị và ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T07:39:53.0509748' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (116, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu đặt lịch khám mới', N'Bệnh nhân Bệnh nhân 1 vừa đặt lịch khám vào 20/07/2026 10:39. Vui lòng xem xét và xác nhận.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-20T07:40:04.7310666' AS DateTime2), 1, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (118, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác sĩ Bác sĩ 1 đã xếp lịch tái khám vào 20/07/2026 10:22 tại dsadsa232.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:23:29.5084759' AS DateTime2), 2, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (119, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác sĩ Bác sĩ 1 đã xếp lịch tái khám vào 20/07/2026 12:25 tại dsads.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:25:37.9516987' AS DateTime2), 2, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (120, N'PATIENT', 31, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác sĩ Bác sĩ 1 vừa cập nhật phác đồ điều trị & danh mục thuốc của bạn. Vui lòng kiểm tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:38:11.4556580' AS DateTime2), 31, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (121, N'PATIENT', 31, N'SYSTEM', N'Cập nhật phác đồ điều trị mới', N'Bác sĩ Bác sĩ 1 vừa cập nhật phác đồ điều trị & danh mục thuốc của bạn. Vui lòng kiểm tra.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:45:29.4060340' AS DateTime2), 31, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (122, N'PATIENT', 0, N'SYSTEM', N'Lịch khám mới được lên bởi Bác sĩ', N'Bác sĩ Bác sĩ 1 đã xếp lịch tái khám vào 20/07/2026 23:45 tại dá.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:45:47.2745394' AS DateTime2), 2, 1)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (123, N'PATIENT', 0, N'SYSTEM', N'Phản hồi yêu cầu đổi phác đồ', N'Bác sĩ đã từ chối yêu cầu của bạn: đâs', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:46:03.0195018' AS DateTime2), 1, 1)
GO
SET IDENTITY_INSERT [dbo].[notifications] OFF
GO
SET IDENTITY_INSERT [dbo].[nutrition_rules] ON 
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (1, 2, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), NULL, NULL, 2000, N'', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:23:16.6019787' AS DateTime2), CAST(N'2026-06-28T23:39:35.4039638' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (2, 1, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), NULL, NULL, 2000, N'', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:29:42.6118036' AS DateTime2), CAST(N'2026-06-29T07:57:24.5070188' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (4, 2, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), NULL, NULL, 2000, N'', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:39:35.4039638' AS DateTime2), CAST(N'2026-06-28T23:49:50.3835037' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (7, 2, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), CAST(1.00 AS Decimal(7, 2)), CAST(1.00 AS Decimal(7, 2)), 2000, N'1', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:49:50.3835037' AS DateTime2), CAST(N'2026-06-28T23:56:22.6811631' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (8, 2, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), CAST(1.00 AS Decimal(7, 2)), CAST(1.00 AS Decimal(7, 2)), 2000, N'1', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:56:22.6811631' AS DateTime2), CAST(N'2026-06-28T23:58:40.2736029' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (9, 2, 1, 1550, CAST(1.00 AS Decimal(7, 2)), CAST(1.00 AS Decimal(6, 2)), CAST(1.00 AS Decimal(6, 2)), CAST(1.00 AS Decimal(7, 2)), CAST(1.00 AS Decimal(7, 2)), 300, N'1', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:58:40.2736029' AS DateTime2), CAST(N'2026-07-03T09:55:14.5887075' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (10, 1, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), CAST(1.00 AS Decimal(7, 2)), CAST(1.00 AS Decimal(7, 2)), 2000, N'', 0, CAST(N'2026-06-29' AS Date), CAST(N'2026-06-29T07:57:24.5070188' AS DateTime2), CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (11, 2, 1, 1550, CAST(1.00 AS Decimal(7, 2)), CAST(1.00 AS Decimal(6, 2)), CAST(1.00 AS Decimal(6, 2)), CAST(1.00 AS Decimal(7, 2)), CAST(1.00 AS Decimal(7, 2)), 300, N'1', 0, CAST(N'2026-07-03' AS Date), CAST(N'2026-07-03T09:55:14.5887075' AS DateTime2), CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (17, 30, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), CAST(10.00 AS Decimal(7, 2)), CAST(10.00 AS Decimal(7, 2)), 2000, N'', 0, CAST(N'2026-07-07' AS Date), CAST(N'2026-07-07T15:14:10.2989295' AS DateTime2), CAST(N'2026-07-10T21:28:51.7382347' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (18, 2, 1, 1550, CAST(1.00 AS Decimal(7, 2)), CAST(1.00 AS Decimal(6, 2)), CAST(1.00 AS Decimal(6, 2)), CAST(1.00 AS Decimal(7, 2)), CAST(1.00 AS Decimal(7, 2)), 300, N'1', 1, CAST(N'2026-07-07' AS Date), CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2), CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (19, 1, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), CAST(1.00 AS Decimal(7, 2)), CAST(1.00 AS Decimal(7, 2)), 2000, N'dsadsadsadsadsadsadsdsa', 1, CAST(N'2026-07-07' AS Date), CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2), CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (20, 30, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), CAST(10.00 AS Decimal(7, 2)), CAST(10.00 AS Decimal(7, 2)), 2000, N'', 1, CAST(N'2026-07-10' AS Date), CAST(N'2026-07-10T21:28:51.7382347' AS DateTime2), CAST(N'2026-07-10T21:28:51.7382347' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (21, 31, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), CAST(7.00 AS Decimal(7, 2)), CAST(11.00 AS Decimal(7, 2)), 2000, N'', 0, CAST(N'2026-07-18' AS Date), CAST(N'2026-07-18T14:22:47.5313628' AS DateTime2), CAST(N'2026-07-20T08:38:11.4556580' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (22, 31, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), CAST(7.00 AS Decimal(7, 2)), CAST(11.00 AS Decimal(7, 2)), 2000, N'', 0, CAST(N'2026-07-20' AS Date), CAST(N'2026-07-20T08:38:11.4556580' AS DateTime2), CAST(N'2026-07-20T08:45:29.4060340' AS DateTime2))
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (23, 31, 1, 2000, CAST(250.00 AS Decimal(7, 2)), CAST(5.00 AS Decimal(6, 2)), CAST(25.00 AS Decimal(6, 2)), CAST(7.00 AS Decimal(7, 2)), CAST(11.00 AS Decimal(7, 2)), 2000, N'', 1, CAST(N'2026-07-20' AS Date), CAST(N'2026-07-20T08:45:29.4060340' AS DateTime2), CAST(N'2026-07-20T08:45:29.4060340' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[nutrition_rules] OFF
GO
SET IDENTITY_INSERT [dbo].[otp_codes] ON 
GO
INSERT [dbo].[otp_codes] ([id], [email], [otp_code], [otp_type], [expires_at], [is_used], [created_at]) VALUES (1, N'domanht11@gmail.com', N'837660', N'REGISTRATION', CAST(N'2026-07-12T22:48:28.4274868' AS DateTime2), 0, CAST(N'2026-07-12T22:43:28.4274868' AS DateTime2))
GO
INSERT [dbo].[otp_codes] ([id], [email], [otp_code], [otp_type], [expires_at], [is_used], [created_at]) VALUES (2, N'domanht13@gmail.com', N'560550', N'REGISTRATION', CAST(N'2026-07-12T22:50:38.7957273' AS DateTime2), 1, CAST(N'2026-07-12T22:45:38.7957273' AS DateTime2))
GO
INSERT [dbo].[otp_codes] ([id], [email], [otp_code], [otp_type], [expires_at], [is_used], [created_at]) VALUES (3, N'domanht14@gmail.com', N'438352', N'REGISTRATION', CAST(N'2026-07-12T22:54:23.1345317' AS DateTime2), 0, CAST(N'2026-07-12T22:49:23.1345317' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[otp_codes] OFF
GO
SET IDENTITY_INSERT [dbo].[patient_medications] ON 
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (2, 1, N'Metformin 500mg', N'1 viên', N'08:00', 0, CAST(N'2026-06-27T22:28:29.8633333' AS DateTime2), CAST(N'2026-06-28T23:29:42.6118036' AS DateTime2), NULL)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (3, 2, N'Metformin 500mg', N'1 viên', N'08:00', 0, CAST(N'2026-06-27T22:29:13.3733333' AS DateTime2), CAST(N'2026-06-28T23:23:16.6019787' AS DateTime2), NULL)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (4, 2, N'Metformin 500mg', N'1 viên', N'08:00', 0, CAST(N'2026-06-28T23:23:16.6019787' AS DateTime2), CAST(N'2026-06-28T23:39:35.4039638' AS DateTime2), 1)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (5, 1, N'Metformin 500mg', N'1 viên', N'08:00', 0, CAST(N'2026-06-28T23:29:42.6118036' AS DateTime2), CAST(N'2026-06-29T07:57:24.5070188' AS DateTime2), 2)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (6, 2, N'Metformin 500mg', N'1 viên', N'08:00', 0, CAST(N'2026-06-28T23:39:35.4039638' AS DateTime2), CAST(N'2026-06-28T23:49:50.3835037' AS DateTime2), 4)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (7, 2, N'Metformin 500mg', N'1 viên', N'08:00', 0, CAST(N'2026-06-28T23:49:50.3835037' AS DateTime2), CAST(N'2026-06-28T23:56:22.6811631' AS DateTime2), 7)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (8, 2, N'Metformin 500mg', N'1 viên', N'08:00', 0, CAST(N'2026-06-28T23:56:22.6811631' AS DateTime2), CAST(N'2026-06-28T23:58:40.2736029' AS DateTime2), 8)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (9, 2, N'Metformin 500mg', N'1 viên', N'08:00', 0, CAST(N'2026-06-28T23:58:40.2736029' AS DateTime2), CAST(N'2026-07-03T09:55:14.5887075' AS DateTime2), 9)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (10, 1, N'Metformin 500mg', N'1 viên', N'08:00', 0, CAST(N'2026-06-29T07:57:24.5070188' AS DateTime2), CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2), 10)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (11, 2, N'Metformin 500mg', N'1 viên', N'08:00', 0, CAST(N'2026-07-03T09:55:14.5887075' AS DateTime2), CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2), 11)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (12, 30, N'10', N'10', N'08:00', 0, CAST(N'2026-07-07T15:14:10.2989295' AS DateTime2), CAST(N'2026-07-10T21:28:51.7382347' AS DateTime2), 17)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (13, 2, N'Metformin 500mg', N'1 viên', N'08:00', 1, CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2), CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2), 18)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (14, 2, N'dsadasd', N'sadsadsa', N'08:00', 1, CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2), CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2), 18)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (15, 1, N'Metformin 500mg', N'1 viên', N'08:00', 1, CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2), CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2), 19)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (16, 1, N'dsadsadsa', N'đâsdsadasdsad', N'09:00', 1, CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2), CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2), 19)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (17, 30, N'10', N'10', N'08:00', 1, CAST(N'2026-07-10T21:28:51.7382347' AS DateTime2), CAST(N'2026-07-10T21:28:51.7382347' AS DateTime2), 20)
GO
SET IDENTITY_INSERT [dbo].[patient_medications] OFF
GO
SET IDENTITY_INSERT [dbo].[patients] ON 
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (1, 13, 1, 1, 3, N'PAT001', N'Bệnh nhân 1', NULL, NULL, N'080000001', NULL, NULL, NULL, N'TREATING', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.3634049' AS DateTime2), CAST(N'2026-06-27T16:13:53.3634049' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (2, 14, 1, 1, 3, N'PAT002', N'Bệnh nhân 2', NULL, NULL, N'080000002', NULL, NULL, NULL, N'TREATING', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5597417' AS DateTime2), CAST(N'2026-06-27T16:13:53.5597417' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (3, 15, 1, 1, NULL, N'PAT003', N'Bệnh nhân 3', NULL, NULL, N'080000003', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5607683' AS DateTime2), CAST(N'2026-06-27T16:13:53.5607683' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (4, 16, 1, 1, 1, N'PAT004', N'Bệnh nhân 4', NULL, NULL, N'080000004', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5617492' AS DateTime2), CAST(N'2026-06-27T16:13:53.5617492' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (5, 17, 1, 2, NULL, N'PAT005', N'Bệnh nhân 5', NULL, NULL, N'080000005', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5617492' AS DateTime2), CAST(N'2026-06-27T16:13:53.5617492' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (6, 18, 1, 2, NULL, N'PAT006', N'Bệnh nhân 6', NULL, NULL, N'080000006', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5627564' AS DateTime2), CAST(N'2026-06-27T16:13:53.5627564' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (7, 19, 1, 2, NULL, N'PAT007', N'Bệnh nhân 7', NULL, NULL, N'080000007', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5627564' AS DateTime2), CAST(N'2026-06-27T16:13:53.5627564' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (8, 20, 1, 2, NULL, N'PAT008', N'Bệnh nhân 8', NULL, NULL, N'080000008', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5638636' AS DateTime2), CAST(N'2026-06-27T16:13:53.5638636' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (9, 21, 1, 3, NULL, N'PAT009', N'Bệnh nhân 9', NULL, NULL, N'080000009', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5647581' AS DateTime2), CAST(N'2026-06-27T16:13:53.5647581' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (10, 22, 1, 3, NULL, N'PAT0010', N'Bệnh nhân 10', NULL, NULL, N'0800000010', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5658471' AS DateTime2), CAST(N'2026-06-27T16:13:53.5658471' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (11, 23, 1, 3, NULL, N'PAT0011', N'Bệnh nhân 11', NULL, NULL, N'0800000011', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5667578' AS DateTime2), CAST(N'2026-06-27T16:13:53.5667578' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (12, 24, 1, 3, NULL, N'PAT0012', N'Bệnh nhân 12', NULL, NULL, N'0800000012', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5667578' AS DateTime2), CAST(N'2026-06-27T16:13:53.5667578' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (13, 25, 1, 4, NULL, N'PAT0013', N'Bệnh nhân 13', NULL, NULL, N'0800000013', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5677535' AS DateTime2), CAST(N'2026-06-27T16:13:53.5677535' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (14, 26, 1, 4, NULL, N'PAT0014', N'Bệnh nhân 14', NULL, NULL, N'0800000014', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5677535' AS DateTime2), CAST(N'2026-06-27T16:13:53.5677535' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (15, 27, 1, 4, NULL, N'PAT0015', N'Bệnh nhân 15', NULL, NULL, N'0800000015', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5688087' AS DateTime2), CAST(N'2026-06-27T16:13:53.5688087' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (16, 28, 1, 4, NULL, N'PAT0016', N'Bệnh nhân 16', NULL, NULL, N'0800000016', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5697600' AS DateTime2), CAST(N'2026-06-27T16:13:53.5697600' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (17, 29, 1, 5, NULL, N'PAT0017', N'Bệnh nhân 17', NULL, NULL, N'0800000017', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5697600' AS DateTime2), CAST(N'2026-06-27T16:13:53.5697600' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (18, 30, 1, 5, NULL, N'PAT0018', N'Bệnh nhân 18', NULL, NULL, N'0800000018', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5709963' AS DateTime2), CAST(N'2026-06-27T16:13:53.5709963' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (19, 31, 1, 5, NULL, N'PAT0019', N'Bệnh nhân 19', NULL, NULL, N'0800000019', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5720062' AS DateTime2), CAST(N'2026-06-27T16:13:53.5720062' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (20, 32, 1, 5, NULL, N'PAT0020', N'Bệnh nhân 20', NULL, NULL, N'0800000020', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-27T16:13:53.5730010' AS DateTime2), CAST(N'2026-06-27T16:13:53.5730010' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (21, 33, 1, 1, NULL, N'PT-73c7c092', N'Bệnh nhân Test 1', NULL, NULL, N'098773c7c0', N'Địa chỉ 1', NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-29T08:35:06.2790852' AS DateTime2), CAST(N'2026-06-29T08:35:06.2790852' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (22, 34, 1, 1, NULL, N'PT-f95e0f39', N'Bệnh nhân Test 2', NULL, NULL, N'0987f95e0f', N'Địa chỉ 2', NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-29T08:35:06.3079779' AS DateTime2), CAST(N'2026-06-29T08:35:06.3079779' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (23, 35, 1, 1, NULL, N'PT-a589ab6f', N'Bệnh nhân Test 3', NULL, NULL, N'0987a589ab', N'Địa chỉ 3', NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-29T08:35:06.3149793' AS DateTime2), CAST(N'2026-06-29T08:35:06.3149793' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (24, 36, 1, 1, NULL, N'PT-a39e1f41', N'Bệnh nhân Test 4', NULL, NULL, N'0987a39e1f', N'Địa chỉ 4', NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-29T08:35:06.3200567' AS DateTime2), CAST(N'2026-06-29T08:35:06.3200567' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (25, 37, 1, 1, 1, N'PT-29dd2bbb', N'Bệnh nhân Test 5', NULL, NULL, N'098729dd2b', N'Địa chỉ 5', NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-29T08:35:06.3287485' AS DateTime2), CAST(N'2026-06-29T08:35:06.3287485' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (26, 38, 1, 1, NULL, N'PT-2bbba644', N'Bệnh nhân Test 6', NULL, NULL, N'09872bbba6', N'Địa chỉ 6', NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-29T08:35:06.3358614' AS DateTime2), CAST(N'2026-06-29T08:35:06.3358614' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (27, 39, 1, 1, NULL, N'PT-9f596ea2', N'Bệnh nhân Test 7', NULL, NULL, N'09879f596e', N'Địa chỉ 7', NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-29T08:35:06.3416320' AS DateTime2), CAST(N'2026-06-29T08:35:06.3416320' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (28, 40, 1, 1, NULL, N'PT-9f4b7b15', N'Bệnh nhân Test 8', NULL, NULL, N'09879f4b7b', N'Địa chỉ 8', NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-29T08:35:06.3473064' AS DateTime2), CAST(N'2026-06-29T08:35:06.3473064' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (29, 41, 1, 1, NULL, N'PT-ddbbd986', N'Bệnh nhân Test 9', NULL, NULL, N'0987ddbbd9', N'Địa chỉ 9', NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-06-29T08:35:06.3543073' AS DateTime2), CAST(N'2026-06-29T08:35:06.3543073' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (30, 42, 1, 1, 1, N'PT-178c761c', N'Bệnh nhân Test 10', NULL, NULL, N'0987178c76', N'Địa chỉ 10', NULL, NULL, N'TREATING', N'ONLINE', 1, NULL, CAST(N'2026-06-29T08:35:06.3593097' AS DateTime2), CAST(N'2026-07-07T15:14:10.2989295' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (31, 43, 1, 1, 1, N'PAT021', N'nguyen van muoi', CAST(N'2009-12-12' AS Date), N'MALE', N'0913231111', NULL, NULL, NULL, N'TREATING', N'ONLINE', 1, CAST(N'2026-07-13T09:44:59.5179381' AS DateTime2), CAST(N'2026-07-12T22:43:28.3788202' AS DateTime2), CAST(N'2026-07-13T09:44:59.5179381' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (32, 44, 1, 1, 1, NULL, N'nguyen van muoi', CAST(N'2009-07-09' AS Date), N'MALE', N'0913231112', NULL, NULL, NULL, N'TREATING', N'ONLINE', 1, CAST(N'2026-07-12T22:50:15.8043759' AS DateTime2), CAST(N'2026-07-12T22:45:38.7656470' AS DateTime2), CAST(N'2026-07-12T22:50:15.8043759' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (33, 45, 1, NULL, NULL, NULL, N'nguyen van muoi', CAST(N'2026-07-11' AS Date), N'MALE', N'0913231114', NULL, NULL, NULL, N'NEW', N'ONLINE', 1, NULL, CAST(N'2026-07-12T22:49:23.1105409' AS DateTime2), CAST(N'2026-07-12T22:49:23.1105409' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[patients] OFF
GO
SET IDENTITY_INSERT [dbo].[treatment_plans] ON 
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (1, 2, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, N'', N'', N'', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:23:16.6019787' AS DateTime2), CAST(N'2026-06-28T23:39:35.4039638' AS DateTime2), NULL, NULL, NULL, NULL, NULL)
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (2, 1, 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, N'', N'', N'', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:29:42.6118036' AS DateTime2), CAST(N'2026-06-29T07:57:24.5070188' AS DateTime2), NULL, NULL, NULL, NULL, NULL)
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (4, 2, 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, N'', N'', N'', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:39:35.4039638' AS DateTime2), CAST(N'2026-06-28T23:49:50.3835037' AS DateTime2), NULL, NULL, NULL, NULL, NULL)
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (7, 2, 1, 7, 1, 1, CAST(1.00 AS Numeric(6, 2)), CAST(1.00 AS Numeric(5, 2)), CAST(1.00 AS Numeric(5, 2)), NULL, NULL, N'', N'1', N'1', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:49:50.3835037' AS DateTime2), CAST(N'2026-06-28T23:56:22.6811631' AS DateTime2), 1, CAST(1.00 AS Numeric(6, 2)), CAST(1.00 AS Numeric(5, 2)), 1, CAST(1.00 AS Numeric(5, 2)))
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (8, 2, 1, 8, 1, 1, CAST(1.00 AS Numeric(6, 2)), CAST(1.00 AS Numeric(5, 2)), CAST(1.00 AS Numeric(5, 2)), NULL, NULL, N'', N'1', N'1', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:56:22.6811631' AS DateTime2), CAST(N'2026-06-28T23:58:40.2736029' AS DateTime2), 1, CAST(1.00 AS Numeric(6, 2)), CAST(1.00 AS Numeric(5, 2)), 1, CAST(1.00 AS Numeric(5, 2)))
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (9, 2, 1, 9, 1, 111, CAST(11.00 AS Numeric(6, 2)), CAST(1.00 AS Numeric(5, 2)), CAST(1.00 AS Numeric(5, 2)), NULL, NULL, N'', N'1', N'1', 0, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T23:58:40.2736029' AS DateTime2), CAST(N'2026-07-03T09:55:14.5887075' AS DateTime2), 1, CAST(1.00 AS Numeric(6, 2)), CAST(1.00 AS Numeric(5, 2)), 1, CAST(1.00 AS Numeric(5, 2)))
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (10, 1, 1, 10, 1, 1, CAST(1.00 AS Numeric(6, 2)), CAST(1.00 AS Numeric(5, 2)), CAST(1.00 AS Numeric(5, 2)), NULL, NULL, N'', N'', N'', 0, CAST(N'2026-06-29' AS Date), CAST(N'2026-06-29T07:57:24.5070188' AS DateTime2), CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2), 1, CAST(1.00 AS Numeric(6, 2)), CAST(1.00 AS Numeric(5, 2)), 1, CAST(1.00 AS Numeric(5, 2)))
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (11, 2, 1, 11, 1, 111, CAST(11.00 AS Numeric(6, 2)), CAST(1.00 AS Numeric(5, 2)), CAST(1.00 AS Numeric(5, 2)), NULL, NULL, N'', N'1', N'1', 0, CAST(N'2026-07-03' AS Date), CAST(N'2026-07-03T09:55:14.5887075' AS DateTime2), CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2), 1, CAST(1.00 AS Numeric(6, 2)), CAST(1.00 AS Numeric(5, 2)), 1, CAST(1.00 AS Numeric(5, 2)))
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (17, 30, 1, 17, 100, 33, CAST(10.00 AS Numeric(6, 2)), CAST(10.00 AS Numeric(5, 2)), CAST(10.00 AS Numeric(5, 2)), NULL, NULL, N'10', N'10', N'10', 0, CAST(N'2026-07-07' AS Date), CAST(N'2026-07-07T15:14:10.2989295' AS DateTime2), CAST(N'2026-07-10T21:28:51.7382347' AS DateTime2), 30, CAST(10.00 AS Numeric(6, 2)), CAST(10.00 AS Numeric(5, 2)), 80, CAST(10.00 AS Numeric(5, 2)))
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (18, 2, 1, 18, 80, 30, CAST(10.00 AS Numeric(6, 2)), CAST(10.00 AS Numeric(5, 2)), CAST(10.00 AS Numeric(5, 2)), NULL, NULL, N'dsadsad', N'1đâsdasdsad', N'1sadasdasdasdsa', 1, CAST(N'2026-07-07' AS Date), CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2), CAST(N'2026-07-07T15:23:45.5410289' AS DateTime2), 30, CAST(20.00 AS Numeric(6, 2)), CAST(10.00 AS Numeric(5, 2)), 100, CAST(10.00 AS Numeric(5, 2)))
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (19, 1, 1, 19, 100, 30, CAST(20.00 AS Numeric(6, 2)), CAST(10.00 AS Numeric(5, 2)), CAST(10.00 AS Numeric(5, 2)), NULL, NULL, N'sadsad', N'dsadsad', N'sadsadsadsa', 1, CAST(N'2026-07-07' AS Date), CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2), CAST(N'2026-07-07T15:27:41.0573071' AS DateTime2), 40, CAST(20.00 AS Numeric(6, 2)), CAST(20.00 AS Numeric(5, 2)), 120, CAST(20.00 AS Numeric(5, 2)))
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (20, 30, 1, 20, 100, 33, CAST(10.00 AS Numeric(6, 2)), CAST(10.00 AS Numeric(5, 2)), CAST(10.00 AS Numeric(5, 2)), NULL, NULL, N'10', N'10', N'10', 1, CAST(N'2026-07-10' AS Date), CAST(N'2026-07-10T21:28:51.7382347' AS DateTime2), CAST(N'2026-07-10T21:28:51.7382347' AS DateTime2), 30, CAST(10.00 AS Numeric(6, 2)), CAST(10.00 AS Numeric(5, 2)), 80, CAST(10.00 AS Numeric(5, 2)))
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (21, 31, 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, N'', N'', N'', 0, CAST(N'2026-07-18' AS Date), CAST(N'2026-07-18T14:22:47.5313628' AS DateTime2), CAST(N'2026-07-20T08:38:11.4556580' AS DateTime2), NULL, NULL, NULL, NULL, NULL)
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (22, 31, 1, 22, NULL, NULL, NULL, NULL, NULL, NULL, NULL, N'', N'', N'', 0, CAST(N'2026-07-20' AS Date), CAST(N'2026-07-20T08:38:11.4556580' AS DateTime2), CAST(N'2026-07-20T08:45:29.4060340' AS DateTime2), NULL, NULL, NULL, NULL, NULL)
GO
INSERT [dbo].[treatment_plans] ([id], [patient_id], [doctor_id], [nutrition_rule_id], [target_systolic_bp], [target_diastolic_bp], [target_fasting_glucose], [target_hba1c], [target_weight_kg], [target_steps], [target_sleep_hours], [medical_order], [exercise_goal], [additional_notes], [is_current], [effective_from], [created_at], [updated_at], [baseline_diastolic_bp], [baseline_fasting_glucose], [baseline_hba1c], [baseline_systolic_bp], [baseline_weight_kg]) VALUES (23, 31, 1, 23, NULL, NULL, NULL, NULL, NULL, NULL, NULL, N'', N'', N'', 1, CAST(N'2026-07-20' AS Date), CAST(N'2026-07-20T08:45:29.4060340' AS DateTime2), CAST(N'2026-07-20T08:45:29.4060340' AS DateTime2), NULL, NULL, NULL, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[treatment_plans] OFF
GO
SET IDENTITY_INSERT [dbo].[water_logs] ON 
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (8, 1, CAST(N'2026-06-20' AS Date), 2200)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (9, 1, CAST(N'2026-06-21' AS Date), 2100)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (10, 1, CAST(N'2026-06-22' AS Date), 2000)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (11, 1, CAST(N'2026-06-23' AS Date), 1900)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (12, 1, CAST(N'2026-06-24' AS Date), 1800)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (13, 1, CAST(N'2026-06-25' AS Date), 1700)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (14, 1, CAST(N'2026-06-26' AS Date), 1600)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (15, 2, CAST(N'2026-06-20' AS Date), 2200)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (16, 2, CAST(N'2026-06-21' AS Date), 2100)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (17, 2, CAST(N'2026-06-22' AS Date), 2000)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (18, 2, CAST(N'2026-06-23' AS Date), 1900)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (19, 2, CAST(N'2026-06-24' AS Date), 1800)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (20, 2, CAST(N'2026-06-25' AS Date), 1700)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (21, 2, CAST(N'2026-06-26' AS Date), 1600)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml]) VALUES (22, 1, CAST(N'2026-07-07' AS Date), 3250)
GO
SET IDENTITY_INSERT [dbo].[water_logs] OFF
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_accounts_email]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [UQ_accounts_email] UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_alerts_doctor_unresolved]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_alerts_doctor_unresolved] ON [dbo].[alerts]
(
	[doctor_id] ASC,
	[alert_level] DESC,
	[triggered_at] DESC
)
INCLUDE([patient_id],[alert_color],[metric_type],[alert_message]) 
WHERE ([is_resolved]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_alerts_hospital_overview]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_alerts_hospital_overview] ON [dbo].[alerts]
(
	[is_resolved] ASC,
	[alert_level] ASC,
	[triggered_at] DESC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_appointments_doctor_pending]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_appointments_doctor_pending] ON [dbo].[appointments]
(
	[doctor_id] ASC,
	[status] ASC,
	[appointment_time] ASC
)
INCLUDE([patient_id],[appointment_type],[created_by]) 
WHERE ([status]='PENDING')
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_appointments_patient_upcoming]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_appointments_patient_upcoming] ON [dbo].[appointments]
(
	[patient_id] ASC,
	[appointment_time] ASC
)
INCLUDE([doctor_id],[status],[appointment_type],[location]) 
WHERE ([status] IN ('PENDING', 'ACCEPTED'))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_audit_trails_actor_action]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_audit_trails_actor_action] ON [dbo].[audit_trails]
(
	[actor_type] ASC,
	[actor_id] ASC,
	[created_at] DESC
)
INCLUDE([action],[target_table],[target_record_id]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_audit_trails_target]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_audit_trails_target] ON [dbo].[audit_trails]
(
	[target_table] ASC,
	[target_record_id] ASC,
	[created_at] DESC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_daily_health_logs_patient_date]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_daily_health_logs_patient_date] ON [dbo].[daily_health_logs]
(
	[patient_id] ASC,
	[log_date] DESC
)
INCLUDE([log_type],[systolic_bp],[diastolic_bp],[heart_rate],[glucose_level],[alert_level],[is_alert_processed]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_daily_health_logs_unprocessed_alerts]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_daily_health_logs_unprocessed_alerts] ON [dbo].[daily_health_logs]
(
	[patient_id] ASC,
	[alert_level] ASC,
	[is_alert_processed] ASC
)
INCLUDE([log_date],[log_time],[systolic_bp],[diastolic_bp],[glucose_level]) 
WHERE ([is_alert_processed]=(0) AND [alert_level] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_diet_logs_patient_date]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_diet_logs_patient_date] ON [dbo].[diet_logs]
(
	[patient_id] ASC,
	[log_date] DESC
)
INCLUDE([food_id],[meal_type],[quantity_g]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_disease_profiles_profile_code]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[disease_profiles] ADD  CONSTRAINT [UQ_disease_profiles_profile_code] UNIQUE NONCLUSTERED 
(
	[profile_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_doctors_account_id]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [UQ_doctors_account_id] UNIQUE NONCLUSTERED 
(
	[account_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_doctors_doctor_code]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [UQ_doctors_doctor_code] UNIQUE NONCLUSTERED 
(
	[doctor_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_doctors_phone]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [UQ_doctors_phone] UNIQUE NONCLUSTERED 
(
	[phone] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_doctors_available_capacity]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_doctors_available_capacity] ON [dbo].[doctors]
(
	[hospital_id] ASC,
	[current_patient_count] ASC,
	[capacity_limit] ASC
)
INCLUDE([doctor_code],[full_name],[specialty]) 
WHERE ([is_active]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_doctors_workload_monitoring]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_doctors_workload_monitoring] ON [dbo].[doctors]
(
	[is_active] ASC,
	[hospital_id] ASC
)
INCLUDE([doctor_code],[full_name],[current_patient_count],[capacity_limit]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_exercise_logs_patient_date]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_exercise_logs_patient_date] ON [dbo].[exercise_logs]
(
	[patient_id] ASC,
	[log_date] DESC
)
INCLUDE([exercise_type],[duration_minutes],[steps_count]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_foods_dictionary_code]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[foods_dictionary] ADD  CONSTRAINT [UQ_foods_dictionary_code] UNIQUE NONCLUSTERED 
(
	[food_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_foods_dictionary_search]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_foods_dictionary_search] ON [dbo].[foods_dictionary]
(
	[food_name] ASC
)
INCLUDE([food_code],[energy_kcal],[protein_g],[lipid_g],[glucid_g]) 
WHERE ([is_active]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_hospitals_account_id]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[hospitals] ADD  CONSTRAINT [UQ_hospitals_account_id] UNIQUE NONCLUSTERED 
(
	[account_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_hospitals_hospital_code]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[hospitals] ADD  CONSTRAINT [UQ_hospitals_hospital_code] UNIQUE NONCLUSTERED 
(
	[hospital_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_medication_logs_med_date]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_medication_logs_med_date] ON [dbo].[medication_logs]
(
	[patient_medication_id] ASC,
	[log_date] ASC
)
INCLUDE([is_taken],[taken_at]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_notifications_recipient_unread]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_notifications_recipient_unread] ON [dbo].[notifications]
(
	[recipient_type] ASC,
	[recipient_id] ASC,
	[is_read] ASC,
	[created_at] DESC
)
INCLUDE([notification_type],[title]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_otp_codes_validation]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_otp_codes_validation] ON [dbo].[otp_codes]
(
	[email] ASC,
	[otp_type] ASC,
	[is_used] ASC,
	[expires_at] ASC
)
INCLUDE([otp_code]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_patient_medications_patient_active]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_patient_medications_patient_active] ON [dbo].[patient_medications]
(
	[patient_id] ASC,
	[is_active] ASC
)
INCLUDE([medicine_name],[dosage],[scheduled_time]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_patients_account_id]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [UQ_patients_account_id] UNIQUE NONCLUSTERED 
(
	[account_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_patients_phone]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [UQ_patients_phone] UNIQUE NONCLUSTERED 
(
	[phone] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_patients_account_login]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_patients_account_login] ON [dbo].[patients]
(
	[account_id] ASC
)
INCLUDE([id],[status],[is_active]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_patients_doctor_status]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_patients_doctor_status] ON [dbo].[patients]
(
	[doctor_id] ASC,
	[status] ASC
)
INCLUDE([patient_code],[full_name],[phone],[disease_profile_id],[is_active]) 
WHERE ([is_active]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_patients_hospital_overview]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_patients_hospital_overview] ON [dbo].[patients]
(
	[hospital_id] ASC,
	[status] ASC,
	[is_active] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_system_logs_time_level]    Script Date: 7/20/2026 10:19:10 AM ******/
CREATE NONCLUSTERED INDEX [IX_system_logs_time_level] ON [dbo].[system_logs]
(
	[created_at] DESC,
	[log_level] ASC
)
INCLUDE([module_name],[event_code],[related_patient_id]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UK1m207yb9al59w4pdsyk7yr739]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[water_logs] ADD  CONSTRAINT [UK1m207yb9al59w4pdsyk7yr739] UNIQUE NONCLUSTERED 
(
	[patient_id] ASC,
	[log_date] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_patient_water_date]    Script Date: 7/20/2026 10:19:10 AM ******/
ALTER TABLE [dbo].[water_logs] ADD  CONSTRAINT [UQ_patient_water_date] UNIQUE NONCLUSTERED 
(
	[patient_id] ASC,
	[log_date] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [DF_accounts_verified]  DEFAULT ((0)) FOR [is_email_verified]
GO
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [DF_accounts_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [DF_accounts_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [DF_accounts_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[alert_thresholds] ADD  CONSTRAINT [DF_alert_thresholds_scope]  DEFAULT ('HOSPITAL') FOR [scope]
GO
ALTER TABLE [dbo].[alert_thresholds] ADD  CONSTRAINT [DF_alert_thresholds_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[alert_thresholds] ADD  CONSTRAINT [DF_alert_thresholds_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[alerts] ADD  CONSTRAINT [DF_alerts_is_resolved]  DEFAULT ((0)) FOR [is_resolved]
GO
ALTER TABLE [dbo].[alerts] ADD  CONSTRAINT [DF_alerts_triggered_at]  DEFAULT (sysdatetime()) FOR [triggered_at]
GO
ALTER TABLE [dbo].[alerts] ADD  CONSTRAINT [DF_alerts_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_status]  DEFAULT ('PENDING') FOR [status]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_type]  DEFAULT ('CHECKUP') FOR [appointment_type]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_created_by]  DEFAULT ('PATIENT') FOR [created_by]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_reminder_2days]  DEFAULT ((0)) FOR [reminder_sent_2days]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[audit_trails] ADD  CONSTRAINT [DF_audit_trails_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[clinical_records] ADD  CONSTRAINT [DF_clinical_records_is_initial]  DEFAULT ((0)) FOR [is_initial_exam]
GO
ALTER TABLE [dbo].[clinical_records] ADD  CONSTRAINT [DF_clinical_records_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[clinical_records] ADD  CONSTRAINT [DF_clinical_records_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[change_requests] ADD  CONSTRAINT [DF_change_requests_status]  DEFAULT ('PENDING') FOR [status]
GO
ALTER TABLE [dbo].[change_requests] ADD  CONSTRAINT [DF_change_requests_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[change_requests] ADD  CONSTRAINT [DF_change_requests_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[daily_health_logs] ADD  CONSTRAINT [DF_daily_health_logs_input_method]  DEFAULT ('MANUAL') FOR [input_method]
GO
ALTER TABLE [dbo].[daily_health_logs] ADD  CONSTRAINT [DF_daily_health_logs_ocr_valid]  DEFAULT ((0)) FOR [is_ocr_validated]
GO
ALTER TABLE [dbo].[daily_health_logs] ADD  CONSTRAINT [DF_daily_health_logs_is_alert_processed]  DEFAULT ((0)) FOR [is_alert_processed]
GO
ALTER TABLE [dbo].[daily_health_logs] ADD  CONSTRAINT [DF_daily_health_logs_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[diet_logs] ADD  CONSTRAINT [DF_diet_logs_logged_at]  DEFAULT (sysdatetime()) FOR [logged_at]
GO
ALTER TABLE [dbo].[disease_profiles] ADD  CONSTRAINT [DF_disease_profiles_requires_bp]  DEFAULT ((0)) FOR [requires_bp_input]
GO
ALTER TABLE [dbo].[disease_profiles] ADD  CONSTRAINT [DF_disease_profiles_requires_glucose]  DEFAULT ((0)) FOR [requires_glucose_input]
GO
ALTER TABLE [dbo].[disease_profiles] ADD  CONSTRAINT [DF_disease_profiles_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [DF_doctors_capacity_limit]  DEFAULT ((50)) FOR [capacity_limit]
GO
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [DF_doctors_current_patient_count]  DEFAULT ((0)) FOR [current_patient_count]
GO
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [DF_doctors_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [DF_doctors_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [DF_doctors_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[emergency_guides] ADD  CONSTRAINT [DF_emergency_guides_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[emergency_guides] ADD  CONSTRAINT [DF_emergency_guides_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[emergency_guides] ADD  CONSTRAINT [DF_emergency_guides_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[emergency_protocols] ADD  CONSTRAINT [DF_emergency_protocols_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[emergency_protocols] ADD  CONSTRAINT [DF_emergency_protocols_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[emergency_protocols] ADD  CONSTRAINT [DF_emergency_protocols_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[exercise_logs] ADD  CONSTRAINT [DF_exercise_logs_logged_at]  DEFAULT (sysdatetime()) FOR [logged_at]
GO
ALTER TABLE [dbo].[foods_dictionary] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[foods_dictionary] ADD  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[hospitals] ADD  CONSTRAINT [DF_hospitals_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[hospitals] ADD  CONSTRAINT [DF_hospitals_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[hospitals] ADD  CONSTRAINT [DF_hospitals_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[medication_logs] ADD  DEFAULT ((0)) FOR [is_taken]
GO
ALTER TABLE [dbo].[notifications] ADD  CONSTRAINT [DF_notifications_channel]  DEFAULT ('IN_APP') FOR [channel]
GO
ALTER TABLE [dbo].[notifications] ADD  CONSTRAINT [DF_notifications_status]  DEFAULT ('PENDING') FOR [status]
GO
ALTER TABLE [dbo].[notifications] ADD  CONSTRAINT [DF_notifications_is_read]  DEFAULT ((0)) FOR [is_read]
GO
ALTER TABLE [dbo].[notifications] ADD  CONSTRAINT [DF_notifications_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[nutrition_rules] ADD  CONSTRAINT [DF_nutrition_rules_water]  DEFAULT ((2000)) FOR [daily_water_ml]
GO
ALTER TABLE [dbo].[nutrition_rules] ADD  CONSTRAINT [DF_nutrition_rules_is_current]  DEFAULT ((1)) FOR [is_current]
GO
ALTER TABLE [dbo].[nutrition_rules] ADD  CONSTRAINT [DF_nutrition_rules_effective_from]  DEFAULT (CONVERT([date],sysdatetime())) FOR [effective_from]
GO
ALTER TABLE [dbo].[nutrition_rules] ADD  CONSTRAINT [DF_nutrition_rules_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[nutrition_rules] ADD  CONSTRAINT [DF_nutrition_rules_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[otp_codes] ADD  CONSTRAINT [DF_otp_codes_is_used]  DEFAULT ((0)) FOR [is_used]
GO
ALTER TABLE [dbo].[otp_codes] ADD  CONSTRAINT [DF_otp_codes_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[patient_medications] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[patient_medications] ADD  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[patient_medications] ADD  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [DF_patients_status]  DEFAULT ('NEW') FOR [status]
GO
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [DF_patients_registration_source]  DEFAULT ('ONLINE') FOR [registration_source]
GO
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [DF_patients_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [DF_patients_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [DF_patients_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[system_logs] ADD  CONSTRAINT [DF_system_logs_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[treatment_plans] ADD  CONSTRAINT [DF_treatment_plans_is_current]  DEFAULT ((1)) FOR [is_current]
GO
ALTER TABLE [dbo].[treatment_plans] ADD  CONSTRAINT [DF_treatment_plans_effective_from]  DEFAULT (CONVERT([date],sysdatetime())) FOR [effective_from]
GO
ALTER TABLE [dbo].[treatment_plans] ADD  CONSTRAINT [DF_treatment_plans_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[treatment_plans] ADD  CONSTRAINT [DF_treatment_plans_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[water_logs] ADD  DEFAULT ((0)) FOR [amount_ml]
GO
ALTER TABLE [dbo].[alert_thresholds]  WITH CHECK ADD  CONSTRAINT [FK_alert_thresholds_doctor_id] FOREIGN KEY([created_by_doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[alert_thresholds] CHECK CONSTRAINT [FK_alert_thresholds_doctor_id]
GO
ALTER TABLE [dbo].[alert_thresholds]  WITH CHECK ADD  CONSTRAINT [FK_alert_thresholds_hospital_id] FOREIGN KEY([hospital_id])
REFERENCES [dbo].[hospitals] ([id])
GO
ALTER TABLE [dbo].[alert_thresholds] CHECK CONSTRAINT [FK_alert_thresholds_hospital_id]
GO
ALTER TABLE [dbo].[alert_thresholds]  WITH CHECK ADD  CONSTRAINT [FK_alert_thresholds_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[alert_thresholds] CHECK CONSTRAINT [FK_alert_thresholds_patient_id]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [FK_alerts_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [FK_alerts_doctor_id]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [FK_alerts_health_log_id] FOREIGN KEY([health_log_id])
REFERENCES [dbo].[daily_health_logs] ([id])
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [FK_alerts_health_log_id]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [FK_alerts_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [FK_alerts_patient_id]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [FK_alerts_resolved_by_doctor_id] FOREIGN KEY([resolved_by_doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [FK_alerts_resolved_by_doctor_id]
GO
ALTER TABLE [dbo].[appointments]  WITH CHECK ADD  CONSTRAINT [FK_appointments_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[appointments] CHECK CONSTRAINT [FK_appointments_doctor_id]
GO
ALTER TABLE [dbo].[appointments]  WITH CHECK ADD  CONSTRAINT [FK_appointments_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[appointments] CHECK CONSTRAINT [FK_appointments_patient_id]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [FK_clinical_records_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [FK_clinical_records_doctor_id]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [FK_clinical_records_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [FK_clinical_records_patient_id]
GO
ALTER TABLE [dbo].[change_requests]  WITH CHECK ADD  CONSTRAINT [FK_change_requests_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[change_requests] CHECK CONSTRAINT [FK_change_requests_doctor_id]
GO
ALTER TABLE [dbo].[change_requests]  WITH CHECK ADD  CONSTRAINT [FK_change_requests_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[change_requests] CHECK CONSTRAINT [FK_change_requests_patient_id]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [FK_daily_health_logs_alert_processed_by] FOREIGN KEY([alert_processed_by])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [FK_daily_health_logs_alert_processed_by]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [FK_daily_health_logs_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [FK_daily_health_logs_patient_id]
GO
ALTER TABLE [dbo].[diet_logs]  WITH CHECK ADD  CONSTRAINT [FK_diet_logs_food_id] FOREIGN KEY([food_id])
REFERENCES [dbo].[foods_dictionary] ([id])
GO
ALTER TABLE [dbo].[diet_logs] CHECK CONSTRAINT [FK_diet_logs_food_id]
GO
ALTER TABLE [dbo].[diet_logs]  WITH CHECK ADD  CONSTRAINT [FK_diet_logs_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[diet_logs] CHECK CONSTRAINT [FK_diet_logs_patient_id]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [FK_doctors_account_id] FOREIGN KEY([account_id])
REFERENCES [dbo].[accounts] ([id])
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [FK_doctors_account_id]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [FK_doctors_hospital_id] FOREIGN KEY([hospital_id])
REFERENCES [dbo].[hospitals] ([id])
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [FK_doctors_hospital_id]
GO
ALTER TABLE [dbo].[emergency_guides]  WITH CHECK ADD  CONSTRAINT [FK_emergency_guides_hospital_id] FOREIGN KEY([hospital_id])
REFERENCES [dbo].[hospitals] ([id])
GO
ALTER TABLE [dbo].[emergency_guides] CHECK CONSTRAINT [FK_emergency_guides_hospital_id]
GO
ALTER TABLE [dbo].[emergency_protocols]  WITH CHECK ADD  CONSTRAINT [FK_emergency_protocols_hospital_id] FOREIGN KEY([hospital_id])
REFERENCES [dbo].[hospitals] ([id])
GO
ALTER TABLE [dbo].[emergency_protocols] CHECK CONSTRAINT [FK_emergency_protocols_hospital_id]
GO
ALTER TABLE [dbo].[exercise_logs]  WITH CHECK ADD  CONSTRAINT [FK_exercise_logs_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[exercise_logs] CHECK CONSTRAINT [FK_exercise_logs_patient_id]
GO
ALTER TABLE [dbo].[hospitals]  WITH CHECK ADD  CONSTRAINT [FK_hospitals_account_id] FOREIGN KEY([account_id])
REFERENCES [dbo].[accounts] ([id])
GO
ALTER TABLE [dbo].[hospitals] CHECK CONSTRAINT [FK_hospitals_account_id]
GO
ALTER TABLE [dbo].[medication_logs]  WITH CHECK ADD  CONSTRAINT [FK_medication_logs_patient_medications] FOREIGN KEY([patient_medication_id])
REFERENCES [dbo].[patient_medications] ([id])
GO
ALTER TABLE [dbo].[medication_logs] CHECK CONSTRAINT [FK_medication_logs_patient_medications]
GO
ALTER TABLE [dbo].[menus]  WITH CHECK ADD  CONSTRAINT [FK5rmgarlc3mkp11dulyfpf5q64] FOREIGN KEY([treatment_plan_id])
REFERENCES [dbo].[treatment_plans] ([id])
GO
ALTER TABLE [dbo].[menus] CHECK CONSTRAINT [FK5rmgarlc3mkp11dulyfpf5q64]
GO
ALTER TABLE [dbo].[menus]  WITH CHECK ADD  CONSTRAINT [FKlak6ufo91kc4pfihuyfxmh7qc] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[menus] CHECK CONSTRAINT [FKlak6ufo91kc4pfihuyfxmh7qc]
GO
ALTER TABLE [dbo].[menus]  WITH CHECK ADD  CONSTRAINT [FKmrjcb6sm2w2v6ottfxynyar43] FOREIGN KEY([nutrition_rule_id])
REFERENCES [dbo].[nutrition_rules] ([id])
GO
ALTER TABLE [dbo].[menus] CHECK CONSTRAINT [FKmrjcb6sm2w2v6ottfxynyar43]
GO
ALTER TABLE [dbo].[menus]  WITH CHECK ADD  CONSTRAINT [FKrbvog7ulseguj1exj3ydx8ni5] FOREIGN KEY([approved_by_doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[menus] CHECK CONSTRAINT [FKrbvog7ulseguj1exj3ydx8ni5]
GO
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [FKmiuhx5o7tg0vb1nkjhw7y9udi] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [FKmiuhx5o7tg0vb1nkjhw7y9udi]
GO
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [FKsxbhag07yf88eve8uuor8tll1] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [FKsxbhag07yf88eve8uuor8tll1]
GO
ALTER TABLE [dbo].[nutrition_rules]  WITH CHECK ADD  CONSTRAINT [FK_nutrition_rules_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[nutrition_rules] CHECK CONSTRAINT [FK_nutrition_rules_doctor_id]
GO
ALTER TABLE [dbo].[nutrition_rules]  WITH CHECK ADD  CONSTRAINT [FK_nutrition_rules_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[nutrition_rules] CHECK CONSTRAINT [FK_nutrition_rules_patient_id]
GO
ALTER TABLE [dbo].[patient_medications]  WITH CHECK ADD  CONSTRAINT [FK_patient_medications_patients] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[patient_medications] CHECK CONSTRAINT [FK_patient_medications_patients]
GO
ALTER TABLE [dbo].[patient_medications]  WITH CHECK ADD  CONSTRAINT [FKchygdc1ped3rvv0mxw8wurpua] FOREIGN KEY([treatment_plan_id])
REFERENCES [dbo].[treatment_plans] ([id])
GO
ALTER TABLE [dbo].[patient_medications] CHECK CONSTRAINT [FKchygdc1ped3rvv0mxw8wurpua]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [FK_patients_account_id] FOREIGN KEY([account_id])
REFERENCES [dbo].[accounts] ([id])
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [FK_patients_account_id]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [FK_patients_disease_profile_id] FOREIGN KEY([disease_profile_id])
REFERENCES [dbo].[disease_profiles] ([id])
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [FK_patients_disease_profile_id]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [FK_patients_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [FK_patients_doctor_id]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [FK_patients_hospital_id] FOREIGN KEY([hospital_id])
REFERENCES [dbo].[hospitals] ([id])
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [FK_patients_hospital_id]
GO
ALTER TABLE [dbo].[treatment_plans]  WITH CHECK ADD  CONSTRAINT [FK_treatment_plans_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[treatment_plans] CHECK CONSTRAINT [FK_treatment_plans_doctor_id]
GO
ALTER TABLE [dbo].[treatment_plans]  WITH CHECK ADD  CONSTRAINT [FK_treatment_plans_nutrition_rule_id] FOREIGN KEY([nutrition_rule_id])
REFERENCES [dbo].[nutrition_rules] ([id])
GO
ALTER TABLE [dbo].[treatment_plans] CHECK CONSTRAINT [FK_treatment_plans_nutrition_rule_id]
GO
ALTER TABLE [dbo].[treatment_plans]  WITH CHECK ADD  CONSTRAINT [FK_treatment_plans_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[treatment_plans] CHECK CONSTRAINT [FK_treatment_plans_patient_id]
GO
ALTER TABLE [dbo].[water_logs]  WITH CHECK ADD  CONSTRAINT [FK_water_logs_patient] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[water_logs] CHECK CONSTRAINT [FK_water_logs_patient]
GO
ALTER TABLE [dbo].[accounts]  WITH CHECK ADD  CONSTRAINT [CHK_accounts_role] CHECK  (([role]='HOSPITAL_ADMIN' OR [role]='DOCTOR' OR [role]='PATIENT'))
GO
ALTER TABLE [dbo].[accounts] CHECK CONSTRAINT [CHK_accounts_role]
GO
ALTER TABLE [dbo].[alert_thresholds]  WITH CHECK ADD  CONSTRAINT [CHK_alert_thresholds_metric_type] CHECK  (([metric_type]='COMBINED' OR [metric_type]='HEART_RATE' OR [metric_type]='BLOOD_PRESSURE' OR [metric_type]='GLUCOSE'))
GO
ALTER TABLE [dbo].[alert_thresholds] CHECK CONSTRAINT [CHK_alert_thresholds_metric_type]
GO
ALTER TABLE [dbo].[alert_thresholds]  WITH CHECK ADD  CONSTRAINT [CHK_alert_thresholds_scope] CHECK  (([scope]='PATIENT' OR [scope]='HOSPITAL'))
GO
ALTER TABLE [dbo].[alert_thresholds] CHECK CONSTRAINT [CHK_alert_thresholds_scope]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [CHK_alerts_alert_color] CHECK  (([alert_color]='RED' OR [alert_color]='ORANGE' OR [alert_color]='YELLOW' OR [alert_color]='GREEN'))
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [CHK_alerts_alert_color]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [CHK_alerts_alert_level] CHECK  (([alert_level]=(4) OR [alert_level]=(3) OR [alert_level]=(2) OR [alert_level]=(1)))
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [CHK_alerts_alert_level]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [CHK_alerts_metric_type] CHECK  (([metric_type]='BOTH' OR [metric_type]='HEART_RATE' OR [metric_type]='BLOOD_PRESSURE' OR [metric_type]='GLUCOSE'))
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [CHK_alerts_metric_type]
GO
ALTER TABLE [dbo].[appointments]  WITH CHECK ADD  CONSTRAINT [CHK_appointments_created_by] CHECK  (([created_by]='DOCTOR' OR [created_by]='PATIENT'))
GO
ALTER TABLE [dbo].[appointments] CHECK CONSTRAINT [CHK_appointments_created_by]
GO
ALTER TABLE [dbo].[appointments]  WITH CHECK ADD  CONSTRAINT [CHK_appointments_status] CHECK  (([status]='CANCELLED' OR [status]='COMPLETED' OR [status]='REJECTED' OR [status]='ACCEPTED' OR [status]='PENDING'))
GO
ALTER TABLE [dbo].[appointments] CHECK CONSTRAINT [CHK_appointments_status]
GO
ALTER TABLE [dbo].[appointments]  WITH CHECK ADD  CONSTRAINT [CHK_appointments_type] CHECK  (([appointment_type]='FOLLOWUP' OR [appointment_type]='INITIAL' OR [appointment_type]='CHECKUP'))
GO
ALTER TABLE [dbo].[appointments] CHECK CONSTRAINT [CHK_appointments_type]
GO
ALTER TABLE [dbo].[audit_trails]  WITH CHECK ADD  CONSTRAINT [CHK_audit_trails_actor_type] CHECK  (([actor_type]='HOSPITAL_ADMIN' OR [actor_type]='PATIENT' OR [actor_type]='DOCTOR'))
GO
ALTER TABLE [dbo].[audit_trails] CHECK CONSTRAINT [CHK_audit_trails_actor_type]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [CHK_clinical_records_diastolic_bp] CHECK  (([diastolic_bp] IS NULL OR [diastolic_bp]>=(30) AND [diastolic_bp]<=(200)))
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [CHK_clinical_records_diastolic_bp]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [CHK_clinical_records_fasting_glucose] CHECK  (([fasting_glucose] IS NULL OR [fasting_glucose]>=(1.0) AND [fasting_glucose]<=(33.3)))
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [CHK_clinical_records_fasting_glucose]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [CHK_clinical_records_heart_rate] CHECK  (([heart_rate] IS NULL OR [heart_rate]>=(20) AND [heart_rate]<=(300)))
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [CHK_clinical_records_heart_rate]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [CHK_clinical_records_systolic_bp] CHECK  (([systolic_bp] IS NULL OR [systolic_bp]>=(50) AND [systolic_bp]<=(300)))
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [CHK_clinical_records_systolic_bp]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [CHK_clinical_records_weight] CHECK  (([weight_kg] IS NULL OR [weight_kg]>(0) AND [weight_kg]<(500)))
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [CHK_clinical_records_weight]
GO
ALTER TABLE [dbo].[change_requests]  WITH CHECK ADD  CONSTRAINT [CHK_change_requests_request_type] CHECK  (([request_type]='APPOINTMENT' OR [request_type]='DIET' OR [request_type]='SCHEDULE' OR [request_type]='TREATMENT_PLAN'))
GO
ALTER TABLE [dbo].[change_requests] CHECK CONSTRAINT [CHK_change_requests_request_type]
GO
ALTER TABLE [dbo].[change_requests]  WITH CHECK ADD  CONSTRAINT [CHK_change_requests_status] CHECK  (([status]='IN_REVIEW' OR [status]='REJECTED' OR [status]='APPROVED' OR [status]='PENDING'))
GO
ALTER TABLE [dbo].[change_requests] CHECK CONSTRAINT [CHK_change_requests_status]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_alert_level] CHECK  (([alert_level]='RED' OR [alert_level]='ORANGE' OR [alert_level]='YELLOW' OR [alert_level]='GREEN' OR [alert_level] IS NULL))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_alert_level]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_diastolic_bp] CHECK  (([diastolic_bp] IS NULL OR [diastolic_bp]>=(30) AND [diastolic_bp]<=(200)))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_diastolic_bp]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_glucose] CHECK  (([glucose_level] IS NULL OR [glucose_level]>=(1.0) AND [glucose_level]<=(33.3)))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_glucose]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_heart_rate] CHECK  (([heart_rate] IS NULL OR [heart_rate]>=(20) AND [heart_rate]<=(300)))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_heart_rate]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_input_method] CHECK  (([input_method]='OCR' OR [input_method]='MANUAL'))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_input_method]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_log_type] CHECK  (([log_type]='RANDOM' OR [log_type]='EVENING' OR [log_type]='MORNING'))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_log_type]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_systolic_bp] CHECK  (([systolic_bp] IS NULL OR [systolic_bp]>=(50) AND [systolic_bp]<=(300)))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_systolic_bp]
GO
ALTER TABLE [dbo].[diet_logs]  WITH CHECK ADD  CONSTRAINT [CHK_diet_logs_meal_type] CHECK  (([meal_type]='SNACK' OR [meal_type]='DINNER' OR [meal_type]='LUNCH' OR [meal_type]='BREAKFAST'))
GO
ALTER TABLE [dbo].[diet_logs] CHECK CONSTRAINT [CHK_diet_logs_meal_type]
GO
ALTER TABLE [dbo].[diet_logs]  WITH CHECK ADD  CONSTRAINT [CHK_diet_logs_quantity] CHECK  (([quantity_g]>(0)))
GO
ALTER TABLE [dbo].[diet_logs] CHECK CONSTRAINT [CHK_diet_logs_quantity]
GO
ALTER TABLE [dbo].[disease_profiles]  WITH CHECK ADD  CONSTRAINT [CHK_disease_profiles_profile_code] CHECK  (([profile_code]='BOTH' OR [profile_code]='DIABETES' OR [profile_code]='HYPERTENSION'))
GO
ALTER TABLE [dbo].[disease_profiles] CHECK CONSTRAINT [CHK_disease_profiles_profile_code]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [CHK_doctors_capacity_limit] CHECK  (([capacity_limit]>=(1) AND [capacity_limit]<=(500)))
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [CHK_doctors_capacity_limit]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [CHK_doctors_count_not_exceed_limit] CHECK  (([current_patient_count]<=[capacity_limit]))
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [CHK_doctors_count_not_exceed_limit]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [CHK_doctors_current_patient_count] CHECK  (([current_patient_count]>=(0)))
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [CHK_doctors_current_patient_count]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [CHK_doctors_gender] CHECK  (([gender]='OTHER' OR [gender]='FEMALE' OR [gender]='MALE' OR [gender] IS NULL))
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [CHK_doctors_gender]
GO
ALTER TABLE [dbo].[emergency_guides]  WITH CHECK ADD  CONSTRAINT [CHK_emergency_guides_alert_level] CHECK  (([alert_level]='RED' OR [alert_level]='ORANGE' OR [alert_level]='YELLOW' OR [alert_level]='GREEN'))
GO
ALTER TABLE [dbo].[emergency_guides] CHECK CONSTRAINT [CHK_emergency_guides_alert_level]
GO
ALTER TABLE [dbo].[emergency_guides]  WITH CHECK ADD  CONSTRAINT [CHK_emergency_guides_metric_type] CHECK  (([metric_type]='BOTH' OR [metric_type]='BLOOD_PRESSURE' OR [metric_type]='GLUCOSE'))
GO
ALTER TABLE [dbo].[emergency_guides] CHECK CONSTRAINT [CHK_emergency_guides_metric_type]
GO
ALTER TABLE [dbo].[emergency_protocols]  WITH CHECK ADD  CONSTRAINT [CHK_emergency_protocols_condition_type] CHECK  (([condition_type]='HYPERGLYCEMIA' OR [condition_type]='HYPOGLYCEMIA' OR [condition_type]='HYPERTENSIVE_CRISIS'))
GO
ALTER TABLE [dbo].[emergency_protocols] CHECK CONSTRAINT [CHK_emergency_protocols_condition_type]
GO
ALTER TABLE [dbo].[exercise_logs]  WITH CHECK ADD  CONSTRAINT [CHK_exercise_logs_duration] CHECK  (([duration_minutes] IS NULL OR [duration_minutes]>(0)))
GO
ALTER TABLE [dbo].[exercise_logs] CHECK CONSTRAINT [CHK_exercise_logs_duration]
GO
ALTER TABLE [dbo].[exercise_logs]  WITH CHECK ADD  CONSTRAINT [CHK_exercise_logs_steps] CHECK  (([steps_count] IS NULL OR [steps_count]>=(0)))
GO
ALTER TABLE [dbo].[exercise_logs] CHECK CONSTRAINT [CHK_exercise_logs_steps]
GO
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [CHK_notifications_channel] CHECK  (([channel]='SMS' OR [channel]='EMAIL' OR [channel]='PUSH' OR [channel]='IN_APP'))
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [CHK_notifications_channel]
GO
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [CHK_notifications_recipient_type] CHECK  (([recipient_type]='HOSPITAL_ADMIN' OR [recipient_type]='DOCTOR' OR [recipient_type]='PATIENT'))
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [CHK_notifications_recipient_type]
GO
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [CHK_notifications_status] CHECK  (([status]='READ' OR [status]='FAILED' OR [status]='SENT' OR [status]='PENDING'))
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [CHK_notifications_status]
GO
ALTER TABLE [dbo].[nutrition_rules]  WITH CHECK ADD  CONSTRAINT [CHK_nutrition_rules_calories] CHECK  (([max_calories_per_day]>=(500) AND [max_calories_per_day]<=(5000)))
GO
ALTER TABLE [dbo].[nutrition_rules] CHECK CONSTRAINT [CHK_nutrition_rules_calories]
GO
ALTER TABLE [dbo].[nutrition_rules]  WITH CHECK ADD  CONSTRAINT [CHK_nutrition_rules_salt] CHECK  (([max_salt_g]>=(0) AND [max_salt_g]<=(20)))
GO
ALTER TABLE [dbo].[nutrition_rules] CHECK CONSTRAINT [CHK_nutrition_rules_salt]
GO
ALTER TABLE [dbo].[otp_codes]  WITH CHECK ADD  CONSTRAINT [CHK_otp_codes_type] CHECK  (([otp_type]='PASSWORD_RESET' OR [otp_type]='REGISTRATION'))
GO
ALTER TABLE [dbo].[otp_codes] CHECK CONSTRAINT [CHK_otp_codes_type]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [CHK_patients_gender] CHECK  (([gender]='OTHER' OR [gender]='FEMALE' OR [gender]='MALE' OR [gender] IS NULL))
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [CHK_patients_gender]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [CHK_patients_registration_source] CHECK  (([registration_source]='CLINIC' OR [registration_source]='ONLINE'))
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [CHK_patients_registration_source]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [CHK_patients_status] CHECK  (([status]='INACTIVE' OR [status]='TREATING' OR [status]='NEW'))
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [CHK_patients_status]
GO
ALTER TABLE [dbo].[system_logs]  WITH CHECK ADD  CONSTRAINT [CHK_system_logs_log_level] CHECK  (([log_level]='CRITICAL' OR [log_level]='ERROR' OR [log_level]='WARNING' OR [log_level]='INFO'))
GO
ALTER TABLE [dbo].[system_logs] CHECK CONSTRAINT [CHK_system_logs_log_level]
GO
/****** Object:  StoredProcedure [dbo].[sp_assign_patient_to_doctor]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================================
-- BƯỚC 4: STORED PROCEDURES
-- ============================================================
 
-- ------------------------------------------------------------
-- SP 1: sp_assign_patient_to_doctor  (không đổi logic)
-- ------------------------------------------------------------
CREATE   PROCEDURE [dbo].[sp_assign_patient_to_doctor]
    @patient_id         INT,
    @doctor_id          INT,
    @actor_id           INT,
    @actor_type         VARCHAR(20),
    @result_message     NVARCHAR(500)   OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
 
    DECLARE @doctor_is_active       BIT;
    DECLARE @doctor_capacity_limit  INT;
    DECLARE @doctor_current_count   INT;
    DECLARE @doctor_full_name       NVARCHAR(255);
    DECLARE @patient_exists         INT;
    DECLARE @patient_current_doctor INT;
 
    BEGIN TRANSACTION;
    BEGIN TRY
 
        SELECT
            @doctor_is_active       = is_active,
            @doctor_capacity_limit  = capacity_limit,
            @doctor_current_count   = current_patient_count,
            @doctor_full_name       = full_name
        FROM doctors WITH (UPDLOCK)
        WHERE id = @doctor_id;
 
        IF @doctor_is_active IS NULL
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Không tìm thấy bác sĩ.';
            RETURN -1;
        END
 
        IF @doctor_is_active = 0
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Tài khoản bác sĩ đã bị vô hiệu hóa.';
            RETURN -2;
        END
 
        IF @doctor_current_count >= @doctor_capacity_limit
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Bác sĩ đã đạt ngưỡng tải tối đa.';
            RETURN -3;
        END
 
        SELECT
            @patient_exists         = id,
            @patient_current_doctor = doctor_id
        FROM patients WITH (UPDLOCK)
        WHERE id = @patient_id;
 
        IF @patient_exists IS NULL
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Không tìm thấy bệnh nhân.';
            RETURN -4;
        END
 
        IF @patient_current_doctor IS NOT NULL AND @patient_current_doctor <> @doctor_id
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Bệnh nhân đã có bác sĩ phụ trách. Cần chuyển giao trước.';
            RETURN -5;
        END
 
        IF @patient_current_doctor = @doctor_id
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Thông báo: Bệnh nhân đã được gán cho bác sĩ này rồi.';
            RETURN 0;
        END
 
        UPDATE patients
        SET doctor_id = @doctor_id, status = 'TREATING', onboarded_at = SYSDATETIME(), updated_at = SYSDATETIME()
        WHERE id = @patient_id;
 
        UPDATE doctors
        SET current_patient_count = current_patient_count + 1, updated_at = SYSDATETIME()
        WHERE id = @doctor_id;
 
        INSERT INTO audit_trails (actor_type, actor_id, action, target_table, target_record_id, new_value)
        VALUES (
            @actor_type, @actor_id, 'ASSIGN_PATIENT_TO_DOCTOR', 'patients', @patient_id,
            N'{"doctor_id":' + CAST(@doctor_id AS VARCHAR) + ', "status":"TREATING"}'
        );
 
        COMMIT TRANSACTION;
        SET @result_message = N'Thành công: Đã gán bệnh nhân cho bác sĩ [' + @doctor_full_name + '].';
        RETURN 0;
 
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        SET @result_message = N'Lỗi hệ thống khi gán bệnh nhân: ' + ERROR_MESSAGE();
        RETURN -99;
    END CATCH;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_available_doctors]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
 
-- ------------------------------------------------------------
-- SP 3: sp_get_available_doctors (không đổi logic)
-- ------------------------------------------------------------
CREATE   PROCEDURE [dbo].[sp_get_available_doctors]
    @hospital_id    INT             = NULL,
    @specialty      NVARCHAR(255)   = NULL
AS
BEGIN
    SET NOCOUNT ON;
 
    SELECT
        d.id                    AS doctor_id,
        d.doctor_code,
        d.full_name             AS doctor_name,
        d.specialty,
        d.phone,
        d.capacity_limit,
        d.current_patient_count,
        d.capacity_limit - d.current_patient_count  AS available_slots,
        CAST(
            (CAST(d.current_patient_count AS FLOAT) / CAST(d.capacity_limit AS FLOAT)) * 100
        AS DECIMAL(5,2))                            AS workload_percentage,
        h.full_name             AS hospital_name
    FROM doctors d
    INNER JOIN hospitals h ON d.hospital_id = h.id
    WHERE
        d.is_active = 1
        AND d.current_patient_count < d.capacity_limit
        AND h.is_active = 1
        AND (@hospital_id IS NULL OR d.hospital_id = @hospital_id)
        AND (@specialty IS NULL OR d.specialty = @specialty)
    ORDER BY
        workload_percentage ASC,
        d.full_name ASC;
 
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_doctor_dashboard]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
 
-- ------------------------------------------------------------
-- SP 5: sp_get_doctor_dashboard
-- [SỬA] Bỏ mức CRITICAL trong phân loại màu dashboard.
-- ------------------------------------------------------------
CREATE   PROCEDURE [dbo].[sp_get_doctor_dashboard]
    @doctor_id  INT,
    @target_date DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
 
    IF @target_date IS NULL SET @target_date = CAST(SYSDATETIME() AS DATE);
 
    SELECT
        p.id                    AS patient_id,
        p.patient_code,
        p.full_name             AS patient_name,
        p.phone,
        dp.profile_code         AS disease_profile,
        latest_log.systolic_bp          AS today_systolic_bp,
        latest_log.diastolic_bp         AS today_diastolic_bp,
        latest_log.glucose_level        AS today_glucose,
        latest_log.alert_level          AS today_alert_level,
        latest_log.log_time             AS last_log_time,
        dashboard_color = ISNULL(latest_log.alert_level, 'GREY'),   -- GREEN/YELLOW/ORANGE/RED hoặc GREY (chưa nhập liệu)
        unresolved_alerts.alert_count   AS unresolved_alert_count,
        morning_log.id                  AS has_morning_log,
        evening_log.id                  AS has_evening_log
    FROM patients p
    LEFT JOIN disease_profiles dp ON p.disease_profile_id = dp.id
    LEFT JOIN (
        SELECT DISTINCT
            dhl.patient_id,
            FIRST_VALUE(dhl.systolic_bp)    OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS systolic_bp,
            FIRST_VALUE(dhl.diastolic_bp)   OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS diastolic_bp,
            FIRST_VALUE(dhl.glucose_level)  OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS glucose_level,
            FIRST_VALUE(dhl.alert_level)    OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS alert_level,
            FIRST_VALUE(dhl.log_time)       OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS log_time
        FROM daily_health_logs dhl
        WHERE dhl.log_date = @target_date
    ) latest_log ON p.id = latest_log.patient_id
    LEFT JOIN (
        SELECT patient_id, COUNT(*) AS alert_count
        FROM alerts
        WHERE is_resolved = 0
        GROUP BY patient_id
    ) unresolved_alerts ON p.id = unresolved_alerts.patient_id
    LEFT JOIN daily_health_logs morning_log
        ON p.id = morning_log.patient_id
        AND morning_log.log_date = @target_date
        AND morning_log.log_type = 'MORNING'
    LEFT JOIN daily_health_logs evening_log
        ON p.id = evening_log.patient_id
        AND evening_log.log_date = @target_date
        AND evening_log.log_type = 'EVENING'
    WHERE
        p.doctor_id     = @doctor_id
        AND p.status    = 'TREATING'
        AND p.is_active = 1
    ORDER BY
        -- Ưu tiên hiển thị: Đỏ -> Cam -> Vàng -> Xám (chưa nhập) -> Xanh
        CASE
            WHEN latest_log.alert_level = 'RED'    THEN 1
            WHEN latest_log.alert_level = 'ORANGE' THEN 2
            WHEN latest_log.alert_level = 'YELLOW' THEN 3
            WHEN latest_log.alert_level IS NULL    THEN 4
            ELSE 5
        END,
        unresolved_alerts.alert_count DESC;
 
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_hospital_overview]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
 
-- ------------------------------------------------------------
-- SP 6: sp_get_hospital_overview
-- [SỬA] Đếm "cảnh báo nghiêm trọng" theo alert_color = 'RED' (không còn CRITICAL).
-- ------------------------------------------------------------
CREATE   PROCEDURE [dbo].[sp_get_hospital_overview]
    @hospital_id    INT
AS
BEGIN
    SET NOCOUNT ON;
 
    SELECT
        h.full_name             AS hospital_name,
        (SELECT COUNT(*) FROM patients p
         WHERE p.hospital_id = @hospital_id AND p.status = 'TREATING' AND p.is_active = 1)
                                AS total_treating_patients,
        (SELECT COUNT(*) FROM patients p
         WHERE p.hospital_id = @hospital_id AND p.status = 'NEW' AND p.is_active = 1)
                                AS total_new_patients,
        -- Số cảnh báo mức RED (cấp cứu) chưa xử lý toàn viện
        (SELECT COUNT(*) FROM alerts a
         INNER JOIN patients p ON a.patient_id = p.id
         WHERE p.hospital_id = @hospital_id AND a.is_resolved = 0 AND a.alert_color = 'RED')
                                AS red_unresolved_alerts,
        (SELECT COUNT(*) FROM doctors d
         WHERE d.hospital_id = @hospital_id AND d.is_active = 1)
                                AS active_doctors_count
    FROM hospitals h
    WHERE h.id = @hospital_id;
 
    SELECT
        d.id                    AS doctor_id,
        d.doctor_code,
        d.full_name             AS doctor_name,
        d.specialty,
        d.current_patient_count,
        d.capacity_limit,
        CAST(
            (CAST(d.current_patient_count AS FLOAT) / CAST(d.capacity_limit AS FLOAT)) * 100
        AS DECIMAL(5,2))        AS workload_percentage,
        workload_status = CASE
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 1.0   THEN 'FULL'
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 0.9   THEN 'CRITICAL'
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 0.7   THEN 'HIGH'
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 0.5   THEN 'MEDIUM'
            ELSE 'LOW'
        END,
        d.is_active
    FROM doctors d
    WHERE d.hospital_id = @hospital_id
    ORDER BY workload_percentage DESC;
 
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_record_daily_health_log]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
 
-- ------------------------------------------------------------
-- SP 4: sp_record_daily_health_log
-- [SỬA] Viết lại toàn bộ logic tính cảnh báo theo:
--   - Đường huyết mmol/L: <4.4 RED(hạ) | 4.4-10 GREEN | 10-16 ORANGE | >16 RED(tăng)
--   - Huyết áp: <120/<80 GREEN | 130-139 hoặc 85-89 YELLOW |
--               140-179 hoặc 90-109 ORANGE | >=180 hoặc >=110 RED
--   - Bỏ hoàn toàn mức CRITICAL — mức cao nhất là RED.
--   - Lấy mức cảnh báo CAO NHẤT giữa huyết áp và đường huyết làm kết quả cuối.
-- ------------------------------------------------------------
CREATE   PROCEDURE [dbo].[sp_record_daily_health_log]
    @patient_id         INT,
    @log_type           VARCHAR(10),
    @input_method       VARCHAR(10),
    @systolic_bp        INT             = NULL,
    @diastolic_bp       INT             = NULL,
    @heart_rate         INT             = NULL,
    @glucose_level       DECIMAL(4,2)    = NULL,     -- mmol/L
    @image_url          VARCHAR(500)    = NULL,
    @patient_notes       NVARCHAR(500)   = NULL,
    @new_log_id          INT             OUTPUT,
    @alert_level_result   VARCHAR(10)     OUTPUT,
    @result_message     NVARCHAR(500)   OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
 
    DECLARE @bp_alert               VARCHAR(10) = 'GREEN';
    DECLARE @glucose_alert          VARCHAR(10) = 'GREEN';
    DECLARE @calculated_alert_level VARCHAR(10) = 'GREEN';
    DECLARE @hospital_id            INT;
    DECLARE @is_ocr_validated       BIT;
 
    SET @is_ocr_validated = CASE WHEN @input_method = 'OCR' THEN 0 ELSE 1 END;
 
    SELECT @hospital_id = hospital_id
    FROM patients
    WHERE id = @patient_id AND is_active = 1;
 
    IF @hospital_id IS NULL
    BEGIN
        SET @result_message = N'Lỗi: Bệnh nhân không tồn tại hoặc đã bị vô hiệu hóa.';
        RETURN -1;
    END
 
    -- ---- Tính mức cảnh báo huyết áp ----
    IF @systolic_bp IS NOT NULL OR @diastolic_bp IS NOT NULL
    BEGIN
        IF (@systolic_bp >= 180 OR @diastolic_bp >= 110)
            SET @bp_alert = 'RED';
        ELSE IF (@systolic_bp BETWEEN 140 AND 179) OR (@diastolic_bp BETWEEN 90 AND 109)
            SET @bp_alert = 'ORANGE';
        ELSE IF (@systolic_bp BETWEEN 130 AND 139) OR (@diastolic_bp BETWEEN 85 AND 89)
            SET @bp_alert = 'YELLOW';
        ELSE IF (ISNULL(@systolic_bp, 0) < 120) AND (ISNULL(@diastolic_bp, 0) < 80)
            SET @bp_alert = 'GREEN';
        ELSE
            -- Vùng trung gian chưa quy định rõ (VD: systolic 120-129 / diastolic 80-84)
            -- tạm xếp mức YELLOW để bệnh nhân lưu ý theo dõi thêm.
            SET @bp_alert = 'YELLOW';
    END
 
    -- ---- Tính mức cảnh báo đường huyết (mmol/L) ----
    IF @glucose_level IS NOT NULL
    BEGIN
        IF @glucose_level < 4.4
            SET @glucose_alert = 'RED';        -- Hạ đường huyết đột ngột
        ELSE IF @glucose_level > 16
            SET @glucose_alert = 'RED';        -- Tăng đường huyết khẩn cấp
        ELSE IF @glucose_level > 10
            SET @glucose_alert = 'ORANGE';     -- Cao
        ELSE
            SET @glucose_alert = 'GREEN';      -- Bình thường (4.4 - 10)
    END
 
    -- ---- Lấy mức cao nhất giữa hai chỉ số ----
    SET @calculated_alert_level = CASE
        WHEN 'RED'    IN (@bp_alert, @glucose_alert) THEN 'RED'
        WHEN 'ORANGE' IN (@bp_alert, @glucose_alert) THEN 'ORANGE'
        WHEN 'YELLOW' IN (@bp_alert, @glucose_alert) THEN 'YELLOW'
        ELSE 'GREEN'
    END;
 
    INSERT INTO daily_health_logs (
        patient_id, log_date, log_time, log_type,
        systolic_bp, diastolic_bp, heart_rate, glucose_level,
        input_method, image_url, is_ocr_validated,
        alert_level, patient_notes
    )
    VALUES (
        @patient_id,
        CAST(SYSDATETIME() AS DATE),
        SYSDATETIME(),
        @log_type,
        @systolic_bp, @diastolic_bp, @heart_rate, @glucose_level,
        @input_method, @image_url,
        @is_ocr_validated,
        @calculated_alert_level,
        @patient_notes
    );
 
    SET @new_log_id = SCOPE_IDENTITY();
 
    -- Theo nghiệp vụ 2.3: chỉ tạo bản ghi alerts (và báo bác sĩ) khi mức ORANGE hoặc RED
    IF @calculated_alert_level IN ('ORANGE', 'RED')
    BEGIN
        DECLARE @doctor_id       INT;
        DECLARE @alert_level_num INT;
 
        SELECT @doctor_id = doctor_id FROM patients WHERE id = @patient_id;
 
        SET @alert_level_num = CASE @calculated_alert_level
            WHEN 'ORANGE' THEN 3
            WHEN 'RED'    THEN 4
        END;
 
        IF @doctor_id IS NOT NULL
        BEGIN
            INSERT INTO alerts (
                patient_id, doctor_id, health_log_id, alert_level, alert_color,
                metric_type, metric_value, threshold_violated, alert_message
            )
            VALUES (
                @patient_id,
                @doctor_id,
                @new_log_id,
                @alert_level_num,
                @calculated_alert_level,
                CASE
                    WHEN @systolic_bp IS NOT NULL AND @glucose_level IS NOT NULL THEN 'BOTH'
                    WHEN @systolic_bp IS NOT NULL THEN 'BLOOD_PRESSURE'
                    ELSE 'GLUCOSE'
                END,
                N'HA: ' + ISNULL(CAST(@systolic_bp AS VARCHAR), 'N/A') + '/' + ISNULL(CAST(@diastolic_bp AS VARCHAR), 'N/A')
                + N' mmHg | Glucose: ' + ISNULL(CAST(@glucose_level AS VARCHAR), 'N/A') + N' mmol/L',
                N'Vượt ngưỡng cảnh báo mức ' + @calculated_alert_level,
                N'Bệnh nhân ID ' + CAST(@patient_id AS VARCHAR)
                + N' có chỉ số vượt ngưỡng: Mức ' + @calculated_alert_level
                + N'. Cần kiểm tra ngay.'
            );
 
            -- Đồng thời ghi vào notifications để gửi thông báo tới bác sĩ
            INSERT INTO notifications (recipient_type, recipient_id, notification_type, title, content, related_entity_type, related_entity_id)
            VALUES (
                'DOCTOR', @doctor_id, 'ALERT',
                N'Cảnh báo sức khỏe mức ' + @calculated_alert_level,
                N'Bệnh nhân ID ' + CAST(@patient_id AS VARCHAR) + N' có chỉ số vượt ngưỡng mức ' + @calculated_alert_level + N'.',
                'DAILY_HEALTH_LOG', @new_log_id
            );
        END
    END
 
    SET @alert_level_result = @calculated_alert_level;
    SET @result_message = N'Thành công: Đã ghi nhận nhật ký sức khỏe. Mức cảnh báo: ' + @calculated_alert_level;
    RETURN 0;
 
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_unassign_patient_from_doctor]    Script Date: 7/20/2026 10:19:10 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
 
-- ------------------------------------------------------------
-- SP 2: sp_unassign_patient_from_doctor (không đổi logic)
-- ------------------------------------------------------------
CREATE   PROCEDURE [dbo].[sp_unassign_patient_from_doctor]
    @patient_id         INT,
    @unassigned_by      INT,
    @actor_type         VARCHAR(20),
    @reason             NVARCHAR(500),
    @result_message     NVARCHAR(500)   OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
 
    DECLARE @current_doctor_id      INT;
    DECLARE @doctor_current_count   INT;
 
    SELECT @current_doctor_id = doctor_id
    FROM patients
    WHERE id = @patient_id;
 
    IF @current_doctor_id IS NULL
    BEGIN
        SET @result_message = N'Thông báo: Bệnh nhân chưa được gán cho bác sĩ nào.';
        RETURN 0;
    END
 
    SELECT @doctor_current_count = current_patient_count
    FROM doctors
    WHERE id = @current_doctor_id;
 
    BEGIN TRANSACTION;
    BEGIN TRY
 
        UPDATE patients
        SET
            doctor_id   = NULL,
            status      = 'INACTIVE',
            updated_at  = SYSDATETIME()
        WHERE id = @patient_id;
 
        UPDATE doctors
        SET
            current_patient_count   = CASE WHEN current_patient_count > 0
                                           THEN current_patient_count - 1
                                           ELSE 0 END,
            updated_at              = SYSDATETIME()
        WHERE id = @current_doctor_id;
 
        INSERT INTO audit_trails (actor_type, actor_id, action, target_table, target_record_id, old_value, notes)
        VALUES (
            @actor_type,
            @unassigned_by,
            'UNASSIGN_PATIENT_FROM_DOCTOR',
            'patients',
            @patient_id,
            N'{"doctor_id":' + CAST(@current_doctor_id AS VARCHAR) + '}',
            @reason
        );
 
        COMMIT TRANSACTION;
 
        SET @result_message = N'Thành công: Đã gỡ bệnh nhân (ID = ' + CAST(@patient_id AS VARCHAR)
                             + N') khỏi bác sĩ (ID = ' + CAST(@current_doctor_id AS VARCHAR)
                             + N'). Dữ liệu y tế được giữ nguyên.';
        RETURN 0;
 
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @result_message = N'Lỗi hệ thống khi gỡ gán bệnh nhân: ' + ERROR_MESSAGE();
        RETURN -99;
    END CATCH;
 
END;
GO
USE [master]
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET  READ_WRITE 
GO
USE [master]
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET  READ_WRITE 
GO


USE RemotePatientMonitoringVer2;
GO

-- ============================================================
-- BƯỚC 1: TẠO BẢNG hospital_admins (NẾU CHƯA TỒN TẠI)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'hospital_admins')
BEGIN
    CREATE TABLE hospital_admins (
        id                  INT             NOT NULL IDENTITY(1,1),
        hospital_id         INT             NOT NULL,               -- Bệnh viện chủ quản (FK trỏ tới hospitals.id)
        account_id          INT             NOT NULL,               -- Tài khoản đăng nhập (FK trỏ tới accounts.id)
        admin_code          VARCHAR(50)     NOT NULL,               -- Mã nhân viên admin (vd: ADM_001)
        full_name           NVARCHAR(150)   NOT NULL,               -- Họ tên người admin
        department          NVARCHAR(100)   NULL,                   -- Phòng ban (CNTT, KHTH, Dinh dưỡng...)
        position            NVARCHAR(100)   NULL,                   -- Chức vụ
        phone               VARCHAR(20)     NULL,                   -- Số điện thoại cá nhân
        admin_role_type     VARCHAR(30)     NOT NULL CONSTRAINT DF_hospital_admins_role DEFAULT 'GENERAL_ADMIN',
        is_active           BIT             NOT NULL CONSTRAINT DF_hospital_admins_active DEFAULT 1,
        created_at          DATETIME2       NOT NULL CONSTRAINT DF_hospital_admins_created_at DEFAULT SYSDATETIME(),
        updated_at          DATETIME2       NOT NULL CONSTRAINT DF_hospital_admins_updated_at DEFAULT SYSDATETIME(),

        CONSTRAINT PK_hospital_admins PRIMARY KEY (id),
        CONSTRAINT UQ_hospital_admins_account_id UNIQUE (account_id),
        CONSTRAINT UQ_hospital_admins_admin_code UNIQUE (admin_code),
        CONSTRAINT FK_hospital_admins_hospital_id FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
        CONSTRAINT FK_hospital_admins_account_id FOREIGN KEY (account_id) REFERENCES accounts(id),
        CONSTRAINT CHK_hospital_admins_role_type CHECK (admin_role_type IN ('SUPER_ADMIN', 'HR_ADMIN', 'MEDICAL_ADMIN', 'IT_ADMIN', 'GENERAL_ADMIN'))
    );
    PRINT '-> 1. Da tao bang hospital_admins thanh cong.';
END
GO

-- ============================================================
-- BƯỚC 2: SAO CHÉP DỮ LIỆU ĐANG CÓ SANG BẢNG MỚI (TRÁNH MẤT MÁT DATA)
-- ============================================================
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hospitals') AND name = 'account_id')
   AND EXISTS (SELECT * FROM sys.tables WHERE name = 'hospital_admins')
BEGIN
    -- Chỉ sao chép nếu bảng hospital_admins mới được tạo và đang trống dữ liệu
    IF NOT EXISTS (SELECT 1 FROM hospital_admins)
    BEGIN
        INSERT INTO hospital_admins (hospital_id, account_id, admin_code, full_name, department, position, phone, admin_role_type, is_active)
        SELECT 
            id, 
            account_id,
'ADM_' + CAST(id AS VARCHAR(10)), -- Tạo mã code mặc định cho admin cũ
            full_name, 
            N'Ban Giám Đốc', 
            N'Quản trị viên', 
            phone, 
            'GENERAL_ADMIN', 
            is_active
        FROM hospitals
        WHERE account_id IS NOT NULL;
        PRINT '-> 2. Da sao chep toan bo tai khoan admin cu sang bang hospital_admins.';
    END
END
GO

-- ============================================================
-- BƯỚC 3: XÓA RÀNG BUỘC KHÓA NGOẠI VÀ UNIQUE CŨ TRÊN BẢNG hospitals
-- ============================================================
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_hospitals_account_id')
BEGIN
    ALTER TABLE hospitals DROP CONSTRAINT FK_hospitals_account_id;
    PRINT '-> 3.1 Da xoa khoa ngoai cu FK_hospitals_account_id tren bang hospitals.';
END
GO

IF EXISTS (SELECT * FROM sys.objects WHERE name = 'UQ_hospitals_account_id' AND parent_object_id = OBJECT_ID('hospitals'))
BEGIN
    ALTER TABLE hospitals DROP CONSTRAINT UQ_hospitals_account_id;
    PRINT '-> 3.2 Da xoa rang buoc UNIQUE UQ_hospitals_account_id tren bang hospitals.';
END
GO

-- ============================================================
-- BƯỚC 4: XÓA CỘT THAM CHIẾU CŨ account_id KHỎI BẢNG hospitals
-- ============================================================
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hospitals') AND name = 'account_id')
BEGIN
    ALTER TABLE hospitals DROP COLUMN account_id;
    PRINT '-> 4. Da xoa cot account_id khoi bang hospitals.';
END
GO
ALTER TABLE doctors ADD date_of_birth DATE

/****** Object:  Table [dbo].[ai_chat_histories]  ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ai_chat_histories](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[account_id] [int] NOT NULL,
	[patient_id] [int] NULL,
	[question] [nvarchar](max) NOT NULL,
	[answer] [nvarchar](max) NOT NULL,
	[citations_json] [nvarchar](max) NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_ai_chat_histories] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

/****** Object:  Table [dbo].[ai_clinical_summaries]  ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ai_clinical_summaries](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NULL,
	[clinical_summary] [nvarchar](max) NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_ai_clinical_summaries] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO